package com.script.content.utils;

import android.util.LruCache;

/**
 * Created by daiepngfei on 2020-09-04
 */
public class UStr {

    public enum Pat {
        /**
         * space = ' '
         */
        SPACES_30(30, ' '),
        /**
         * '-'
         */
        LH_30(30, '-'),
        /**
         *
         */
        HASHTAG_30(30, '#'),
        /**
         *
         */
        STARS_30(30, '*');
        private String value;

        Pat(int l, char c) {
            this.value = UStr.getSpaceLs(l, c);
        }
    }

    private static CS CS = new CS();


    /**
     * getFixedLenString
     *
     * @return
     */
    public static String pfls(Pat pat, String s, int maxLen) {
        return fls(s, pat, maxLen, false, true);
    }

    /**
     * @param s
     * @param maxLen
     *
     * @return
     */
    public static String cfls(Pat pat, String s, int maxLen) {
        return fls(s, pat, maxLen, true, false);
    }

    /**
     * getFixedLenString
     *
     * @return
     */
    public static String sfls(Pat pat, String s, int maxLen) {
        return fls(s, pat, maxLen, false, false);
    }

    /**
     * @param s
     * @param maxLen
     * @param asPrefix
     *
     * @return
     */
    private static String fls(String s, Pat pat, int maxLen, boolean center, boolean asPrefix) {
        if (s == null) {
            return "";
        }
        final int delta = maxLen - s.length();
        final String m = pat.value;
        if (delta > 0 && delta < m.length()) {
            if (center) {
                String cs = CS.get(s);
                if (cs == null) {
                    final int h = delta >>> 1;
                    CS.put(s, m.substring(0, delta - h) + s + m.substring(0, h));
                }
                s = CS.get(s);
            } else {
                final String ap = m.substring(0, delta);
                s = asPrefix ? ap + s : s + ap;
            }
        }
        return s;
    }

    private static String getSpaceLs(int num, char c) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    private static class CS extends LruCache<String, String> {

        /**
         * @param maxSize
         *         for caches that do not override {@link #sizeOf}, this is the maximum number
         *         of entries in the cache. For all other caches, this is the maximum sum of the
         *         sizes of the entries in this cache.
         */
        CS(int maxSize) {
            super(maxSize);
        }

        CS() {
            this(2 * 1024 * 1024);
        }

        @Override
        protected int sizeOf(String key, String value) {
            return key.length() + value.length();
        }
    }
}
