package com.sensor.common;

/**
 * Created by tianyi on 29/08/2017.
 */
public class SegmenterConstants {
    public static final String SEGMENTER_RUNTIME_OUTPUT_PATH = "/sa/runtime/segmenter";
    public static final String SEGMENTER_OUTPUT_PATH = "/sa/data/segmenter";
    public static final String PREDICTOR_OUTPUT_PATH = "/sa/data/predictor";

    public SegmenterConstants() {
    }

    public static enum KUDU_LOAD_STATUS {
        FRESH,
        INGESTING,
        FULL,
        STAGING,
        MOVEDOUT;

        private KUDU_LOAD_STATUS() {
        }

        public String toString() {
            return this.name();
        }
    }

    public static enum SEGMENTER_TYPE {
        ONE_TIME(0),
        ROUTINE(1),
        QUERY_RESULT(2),
        VIRTUAL(3),
        PREDICTOR(4);

        private int value = 0;

        private SEGMENTER_TYPE(int var3) {
            this.value = var3;
        }

        public static SegmenterConstants.SEGMENTER_TYPE valueOf(int var0) {
            switch(var0) {
                case 0:
                    return ONE_TIME;
                case 1:
                    return ROUTINE;
                case 2:
                case 3:
                default:
                    return QUERY_RESULT;
                case 4:
                    return PREDICTOR;
            }
        }

        public int value() {
            return this.value;
        }
    }

    public static enum SEGMENTATION_RESULT {
        UNMATCHED,
        MATCHED;

        private SEGMENTATION_RESULT() {
        }
    }

    public static enum SEGMENTATION_RULE_TYPE {
        PROFILE_RULE,
        EVENT_RULE,
        EVENT_SEQUENCE_RULE;

        private SEGMENTATION_RULE_TYPE() {
        }
    }

    public static enum TABLE_TYPE {
        EVENT_TABLE,
        PROFILE_TABLE,
        SEGMENTATION_TABLE;

        private TABLE_TYPE() {
        }
    }
}
