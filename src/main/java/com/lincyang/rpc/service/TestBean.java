package com.lincyang.rpc.service;

/**
 * @Author lincyang
 * @Date 2019/8/19 10:21 AM
 **/
public class TestBean {
    private String name;
    private Integer age;

    public TestBean(String name,Integer age) {
        this.name = name;
        this.age = age;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "TestBean{" + "name='" + name + '\'' + ", age=" + age + '}';
    }
}
