package com.qian.feather.item;

public class FileInfo {
    private String name;
    private String time;
    private long size;
    public FileInfo() {}
    public FileInfo(Builder builder) {
        this.name = builder.name;
        this.time = builder.time;
        this.size = builder.size;
    }
    public String getName() {
        return name;
    }
    public String getTime() {
        return time;
    }
    public long getSize() {
        return size;
    }

    public static class Builder {
        private String name;
        private String time;
        private long size;
        public Builder() {
            name = "";
            time = "";
            size = 0;
        }
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        public Builder setTime(String time) {
            this.time = time;
            return this;
        }
        public Builder setSize(long size) {
            this.size = size;
            return this;
        }
        public FileInfo build() {
            return new FileInfo(this);
        }
    }
}
