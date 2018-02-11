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
package eu.luminis.jmeter.wssampler;

public enum ComparisonType {

    IsPlain,
    Equals,
    EqualsRegex,
    Contains,
    ContainsRegex,
    StartsWith,
    EndsWith,
    NotEquals,
    NotEqualsRegex,
    NotContains,
    NotContainsRegex,
    NotStartsWith,
    NotEndsWith;

    public boolean isRegexComparison() {
        return this == EqualsRegex || this == ContainsRegex || this == NotContainsRegex || this == NotEqualsRegex;
    }
}
