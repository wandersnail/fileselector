package cn.zfs.fileselector;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by zeng on 2016/12/28.
 * 执行shell命令工具类
 */

class ShellUtils {
    private static final String COMMAND_SU = "su";
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_LINE_END = "\n";


    private ShellUtils() {}

    /*
     * check whether has root permission
     */
    static boolean hasRootPermission() {
        return execCommand("echo root", true).result == 0;
    }


    /*
     * execute shell command, default return result msg
     *
     * @param command command
     * @param isRoot  whether need to run with root
     * @see ShellUtils#execCommand(String[], boolean)
     */
    static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot);
    }

    /*
     * execute shell commands
     *
     * @param commands        command list
     * @param isRoot          whether need to run with root
     * @see ShellUtils#execCommand(String[], boolean)
     */
    static CommandResult execCommand(List<String> commands, boolean isRoot) {
        return execCommand(commands == null ? null : commands.toArray(new String[0]), isRoot);
    }


    /*
     * execute shell commands.
     *
     * @param commands        command array
     * @param isRoot          whether need to run with root
     * @return if isNeedResultMsg is false, {@link CommandResult#successMsg} is null and
     * {@link CommandResult#errorMsg} is null.
     * <br/>
     * if {@link CommandResult#result} is -1, there maybe some excepiton.
     */
    static CommandResult execCommand(String[] commands, boolean isRoot) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        DataOutputStream os = null;
        ReadMsgRunnable successMsgRunnable = null;
        ReadMsgRunnable errorMsgRunnable = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            successMsgRunnable = new ReadMsgRunnable(process.getInputStream());
            errorMsgRunnable = new ReadMsgRunnable(process.getErrorStream());
            new Thread(successMsgRunnable).start();
            new Thread(errorMsgRunnable).start();
            result = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsgRunnable == null ? null : successMsgRunnable.getMsg(),
                errorMsgRunnable == null ? null : errorMsgRunnable.getMsg());
    }
    
    private static class ReadMsgRunnable implements Runnable {
        private StringBuilder sb;
        private InputStream is;
        
        ReadMsgRunnable(InputStream is) {
            this.is = is;
            sb = new StringBuilder();
        }
        
        String getMsg() {
            return sb.toString();
        }
        
        @Override
        public void run() {
            BufferedReader result = null;
            try {
                result = new BufferedReader(new InputStreamReader(is));
                String s;
                while ((s = result.readLine()) != null) {
                    sb.append(s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (result != null) {
                        result.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    static class CommandResult {
        int result;
        String successMsg;
        String errorMsg;


        CommandResult(int result) {
            this.result = result;
        }


        CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}
