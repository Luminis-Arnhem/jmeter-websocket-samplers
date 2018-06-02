package eu.luminis.jmeter.wssampler;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JMeterUtilsTest {

    private JMeterUtils objectUnderTest = new JMeterUtils() {};

    @Test
    public void simpleJMeterVariableShouldBeDetected() {
        String var = "${MYPROPERTY}";
        assertThat(objectUnderTest.stripJMeterVariables(var)).isEmpty();
    }

    @Test
    public void strippingJMeterVariableShouldKeepOtherChars() {
        String var = "123${MYPROPERTY}456";
        assertThat(objectUnderTest.stripJMeterVariables(var)).isEqualTo("123456");
    }

    @Test
    public void jmeterVariableWithDashesAndUnderscoresShouldBeDetected() {
        String var = "${MY_PRO-PER-TY}";
        assertThat(objectUnderTest.stripJMeterVariables(var)).isEmpty();
    }

    @Test
    public void jmeterVariableWithSpacesAndParenthesisShouldBeDetected() {
        String var = "${__Random(1,63, LOTTERY )}";
        assertThat(objectUnderTest.stripJMeterVariables(var)).isEmpty();
    }

    @Test
    public void jmeterVariableContainingUnmatchedBracketAndParenthesisShouldBeAccepted() {
        String var = "${my weird[( property}";
        assertThat(objectUnderTest.stripJMeterVariables(var)).isEmpty();

    }

    @Test
    public void jmeterVariableWithSurplusClosingBraceShouldKeepTheClosingBrace() {
        String var = "${prop}}";
        assertThat(objectUnderTest.stripJMeterVariables(var)).isEqualTo("}");
    }

    @Test
    public void unclosedJmeterVariableShouldNotBeDetectedAsVariable() {
        String var = "${prop";
        assertThat(objectUnderTest.stripJMeterVariables(var)).isEqualTo("${prop");
    }
}
