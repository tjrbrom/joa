package com.x.hhqs.joa.common.rt1fwk.pojo;

public class ValidAttribute {
     String name;
     String regexpr;
     boolean required = false;

     public ValidAttribute(String name, String regexpr, boolean required) {
          this.name = name;
          this.regexpr = regexpr;
          this.required = required;
     }

     public String getName() {
          return name;
     }

     public void setName(String name) {
          this.name = name;
     }

     public String getRegexpr() {
          return regexpr;
     }

     public void setRegexpr(String regexpr) {
          this.regexpr = regexpr;
     }

     public boolean isRequired() {
          return required;
     }

     public void setRequired(boolean required) {
          this.required = required;
     }
}
