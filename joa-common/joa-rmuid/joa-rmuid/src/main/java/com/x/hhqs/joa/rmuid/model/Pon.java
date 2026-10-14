package com.x.hhqs.joa.rmuid.model;

public class Pon {

    private int id;

    private Technology technology;

    public Pon(int id, Technology technology) {
        super();
        this.id = id;
        this.technology = technology;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Technology getTechnology() {
        return technology;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }

    public static enum Technology {
        GPON("GPON", 1),
        XGS("XGS", 2),
        XGS_PON("XGS-PON", 2),
        G25("25G", 3),
        G50("50G", 4),
        EMPTY("", 0);

        String name = "";
        int value = 0;

        Technology(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public static Technology of(String name) {
            for (Technology technology : Technology.values()) {
                if (technology.getName().equalsIgnoreCase(name)) {
                    return technology;
                }
            }

            return EMPTY;
        }

        public static Technology of(int value) {
            for (Technology technology : Technology.values()) {
                if (technology.value == value) {
                    return technology;
                }
            }

            return EMPTY;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

    }

    @Override
    public String toString() {
        return "PON-" + id + "-" + technology.getName();
    }

}
