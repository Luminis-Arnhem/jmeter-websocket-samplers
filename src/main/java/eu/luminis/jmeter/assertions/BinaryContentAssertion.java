/*
 * Copyright © 2016, 2017, 2018 Peter Doornbosch
 *
 * This file is part of JMeter-WebSocket-Samplers, a JMeter add-on for load-testing WebSocket applications.
 *
 * JMeter-WebSocket-Samplers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * JMeter-WebSocket-Samplers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.luminis.jmeter.assertions;

import eu.luminis.jmeter.wssampler.BinaryUtils;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedAssertion;

import java.util.Arrays;

public class BinaryContentAssertion extends AbstractScopedAssertion implements Assertion {

    public enum ComparisonType {
        Equals,
        Contains,
        StartsWith,
        NotEquals,
        NotContains,
        NotStartsWith
    }

    @Override
    public AssertionResult getResult(SampleResult sampleResult) {
        byte[] responseData = sampleResult.getResponseData();
        byte[] comparisonValue = BinaryUtils.parseBinaryString(getComparisonValue());

        AssertionResult result = new AssertionResult(getName());

        switch (getComparisonType()) {
            case Equals:
                boolean equal = Arrays.equals(responseData, comparisonValue);
                result.setFailure(!equal);
                if (!equal)
                    result.setFailureMessage("Response expected to equal " + BinaryUtils.formatBinary(comparisonValue) + "\n" + "Response was: " + BinaryUtils.formatBinary(responseData));
                break;
            case Contains:
                boolean contains = BinaryUtils.contains(responseData, comparisonValue);
                result.setFailure(!contains);
                if (!contains)
                    result.setFailureMessage("Response expected to contain " + BinaryUtils.formatBinary(comparisonValue) + "\n" + "Response was: " + BinaryUtils.formatBinary(responseData));
                break;
            case NotEquals:
                equal = Arrays.equals(responseData, comparisonValue);
                result.setFailure(equal);
                if (equal)
                    result.setFailureMessage("Response expected not to equal " + BinaryUtils.formatBinary(comparisonValue) + "\n" + "Response was: " + BinaryUtils.formatBinary(responseData));
                break;
            case NotContains:
                contains = BinaryUtils.contains(responseData, comparisonValue);
                result.setFailure(contains);
                if (contains)
                    result.setFailureMessage("Response expected not to contain " + BinaryUtils.formatBinary(comparisonValue) + "\n" + "Response was: " + BinaryUtils.formatBinary(responseData));
                break;
            case StartsWith:
                boolean startsWith = comparisonValue.length <= responseData.length && Arrays.equals(comparisonValue, Arrays.copyOf(responseData, comparisonValue.length));
                if (!startsWith)
                    result.setFailureMessage("Response expected to start with " + BinaryUtils.formatBinary(comparisonValue) + "\n" + "Response was: " + BinaryUtils.formatBinary(responseData));
                result.setFailure(!startsWith);
                break;
            case NotStartsWith:
                startsWith = comparisonValue.length <= responseData.length && Arrays.equals(comparisonValue, Arrays.copyOf(responseData, comparisonValue.length));
                if (startsWith)
                    result.setFailureMessage("Response expected not to start with " + BinaryUtils.formatBinary(comparisonValue) + "\n" + "Response was: " + BinaryUtils.formatBinary(responseData));
                result.setFailure(startsWith);
                break;
            default:
                throw new RuntimeException("Program error");
        }

        return result;
    }

    public String getComparisonValue() {
        return getPropertyAsString("compareValue");
    }

    public void setComparisonValue(String comparisonValue) {
        setProperty("compareValue", comparisonValue);
    }

    public ComparisonType getComparisonType() {
         String rawValue = getPropertyAsString("comparisonType");
        if (rawValue.trim().length() > 0)
            return ComparisonType.valueOf(rawValue);
        else {
            // JMeter file does not (yet) contain this property, return default;
            return ComparisonType.Contains;
        }
    }

    public void setComparisonType(ComparisonType comparisonType) {
        setProperty("comparisonType", comparisonType.toString());
    }
}
