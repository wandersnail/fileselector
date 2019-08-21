package cn.wandersnail.fileselector;

/**
 * date: 2019/8/8 14:48
 * author: zengfansheng
 */
public enum Language {
    SIMPLIFIED_CHINESE(0), TRADITIONAL_CHINESE(1), ENGLISH(2);
    
    int value;

    Language(int value) {
        this.value = value;
    }
}
