package com.gitee.freakchicken.blindjava;

import com.gitee.freakchicken.blindjava.handler.BlindClass;
import com.gitee.freakchicken.blindjava.handler.BlindMethod;
import com.gitee.freakchicken.blindjava.handler.BlindNote;
import com.gitee.freakchicken.blindjava.handler.BlindVariable;
import org.junit.Test;

import java.io.IOException;

/**
 * @author 王红
 * @date2023/11/27 11:18
 * @description
 */
public class TestBlind {


    @Test
    public void test() throws IOException {
        BlindVariable blindVariable = new BlindVariable();
        blindVariable.blindVariable();

        BlindMethod blindMethod = new BlindMethod();
        blindMethod.blindMethod();


        BlindClass blindClass = new BlindClass();
        blindClass.blindClass();

        BlindNote blindNote = new BlindNote();
        blindNote.blindNote();
    }

}
