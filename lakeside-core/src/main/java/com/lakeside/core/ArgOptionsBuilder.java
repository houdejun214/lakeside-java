package com.lakeside.core;

/**
 * Created by dejun on 24/11/14.
 */
public class ArgOptionsBuilder {

    private ArgOptions options = new ArgOptions();


    /**
     * add a new argument description
     * @param name
     * @param required
     * @param help
     */
    public ArgOptionsBuilder addArgument(String name, boolean required, String help){
        options.put(name, new BaseOptions.Option(name,required,help));
        return this;
    }

    /**
     * add a new argument description
     * @param name
     * @param required
     * @param help
     */
    public ArgOptionsBuilder addArgument(String name, String alias, boolean required, String help){
        options.put(name, new BaseOptions.Option(name, alias,required,help));
        return this;
    }


    /**
     * parse arguments list
     * @param args
     * @return
     */
    public ArgOptions parse(String[] args){
        options.parse(args);
        return options;
    }

    /**
     * parse arguments list
     * @return
     */
    public ArgOptions parse(){
        options.parse(null,false);
        return options;
    }

    public static ArgOptionsBuilder create() {
        return new ArgOptionsBuilder();
    }

}
