package io.github.mayubao.kuaichuan.core;


public interface Transferable {



    /**
     *
     * @throws Exception
     */
    void init() throws Exception;


    /**
     *
     * @throws Exception
     */
    void parseHeader() throws Exception;


    /**
     *
     * @throws Exception
     */
    void parseBody() throws Exception;


    /**
     *
     * @throws Exception
     */
    void finish() throws Exception;
}
