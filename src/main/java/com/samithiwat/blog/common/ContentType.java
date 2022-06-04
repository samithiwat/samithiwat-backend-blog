package com.samithiwat.blog.common;

public enum ContentType {
    TEXT{
        @Override
        public String toString() {
            return "text";
        }
    },

    IMAGE{
        @Override
        public String toString() {
            return "image";
        }
    },

    CODE{
        @Override
        public String toString() {
            return "code";
        }
    };
}
