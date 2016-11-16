/**
 * The MIT License
 * Copyright (c) 2016 Techcable
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.techcable.pineapple;

import org.junit.Test;

import static net.techcable.pineapple.SimpleFormatter.format;
import static org.junit.Assert.*;

public class SimpleFormatterTest {
    @SuppressWarnings("PatternValidation")
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInput() {
        format("I am very smart but I like to use invalid data {");
    }

    @Test
    public void testNoArgs() {
        assertEquals(
                "I hate your new-fangled argument system!",
                format("I hate your new-fangled argument system!")
        );
        assertEquals(
                "When I was a kid I had to walk up the hill both ways to get to school!",
                format("When I was a kid I had to walk up the hill both ways to get to school!")
        );
        assertEquals(
                "When I was a kid, we had to get up before we went to bed.",
                format("When I was a kid, we had to get up before we went to bed.")
        );
    }

    @Test
    public void testIncrementalArgs() {
        assertEquals(
                "The letters of the alphabet are: a, b, c",
                format("The letters of the alphabet are: {}, {}, {}", "a", "b", "c")
        );
        assertEquals(
                "10 minus 5 is 5.",
                format("{} minus {} is {}.", 10, 5, 5)
        );
        assertEquals(
                "Who lives in a pineapple under the sea? Sponge Bob Square Pants",
                format("Who lives in a {} under the {}? Sponge {} Square {}", "pineapple", "sea", "Bob", "Pants")
        );
    }

    @Test
    public void testIndexedArguments() {
        assertEquals(
                "The three best things in the world are: cake, pizza, and fruit-rollups. Did I forget to mention cake?",
                format("The three best things in the world are: {}, {}, and {}. Did I forget to mention {0}?", "cake", "pizza", "fruit-rollups")
        );
        assertEquals(
                "Newton and Einstein are the best physicists ever. They are very smart physicists!",
                format("{} and {} are the best {}s ever. They are very smart {2}s!", "Newton", "Einstein", "physicist")
        );
        assertEquals(
                "md_5 and Thinkofdeath run Spigot, which is the successor to the Bukkit project. Spigot releases new versions much faster than the old Bukkit project.",
                format("{} and {} run {}, which is the successor to the {} project. {2} releases new versions much faster than the old {3} project.", "md_5", "Thinkofdeath", "Spigot", "Bukkit")
        );
        assertEquals(
                "My name is dave. I like to do things backwards because I'm crazy. My friends just call me crazy dave.",
                format("My name is {1}. I like to do things backwards because I'm {0}. My friends just call me {0} {1}.", "crazy", "dave")
        );
    }

    @Test
    public void testEscaping() {
        assertEquals(
                "This is the backslash of doom: \\",
                format("This is the backslash of {}: \\\\", "doom")
        );
        assertEquals(
                "Brackets are the most important part of programming. Here is what one looks like: '{'",
                format("Brackets are the most important part of {}. Here is what one looks like: '\\{'", "programming")
        );
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsufficientArguments() {
        format("The answer to life is: {}");
    }

    @Test
    public void testTooManyArguments() {
        assertEquals(
                "The answer to life is: ",
                format("The answer to life is: ", 42)
        );
        assertEquals(
                "Do you want to build a snowman?",
                format("Do you want to {} a snowman?", "build", "skyscraper")
        );
        assertEquals(
                "How much wood could a woodchuck chuck if a woodchuck could chuck wood?",
                format(
                        "How much wood could a {1} {2} if a {1} could {2} wood?",
                        "tacoz",
                        "woodchuck",
                        "chuck"
                )
        );
    }

}
