package com.sensor.queryengine.expression;

/**
 * 用于 处理同名的情况下 自增的字母， a, b, c
 * Created by tianyi on 10/08/2017.
 */
public class AliasGenerator {
    private int id = 0;
    private String prefix = "";
    private static final int MAX_ID = 25;

    public AliasGenerator() {
    }

    /**
     * 自增生成下一个 区分字母
     * @return
     */
    public String nextAlias() {
        char generator = (char)(97 + this.id);
        String str = this.prefix + generator;
        ++ this.id;
        if (this.id > 25) {
            this.id = 0;
            this.prefix = this.prefix + "x";
        }
        return str;
    }

    public void clear() {
        this.id = 0;
        this.prefix = "";
    }

    /**
     * 用于测试
     * @param args
     */
    public static void main(String[] args) {
        AliasGenerator var1 = new AliasGenerator();

        int var2;
        for(var2 = 0; var2 < 100; ++var2) {
            System.out.println(var1.nextAlias());
        }

        var1.clear();

        for(var2 = 0; var2 < 10; ++var2) {
            System.out.println(var1.nextAlias());
        }

    }
}
