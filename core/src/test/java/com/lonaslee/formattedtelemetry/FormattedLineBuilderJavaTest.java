package com.lonaslee.formattedtelemetry;

import org.junit.Test;

public class FormattedLineBuilderJavaTest {
    @Test
    public void test() {
        FormattedLineBuilder lb = new FormattedLineBuilder()
                .spinner(new String[]{"1", "2", "3"}, 10, 0);
        System.out.println(lb);
    }
}
