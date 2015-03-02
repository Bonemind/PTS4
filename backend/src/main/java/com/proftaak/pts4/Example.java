package com.proftaak.pts4;

/**
 * This is an example of the coding style.
 */
class Example {
    /**
     * This is a public String
     */
    public String someString = "";

    /**
     * This is a private String
     */
    private String anotherString = "";

    /**
     * The main constructor, does nothing useful
     */
    public Example() {
        String thisIsAString;
        thisIsAString = "Hello";
        System.out.println(thisIsAString);
    }

    /**
     * This returns some String
     *
     * @param aString The String we want a return value from
     * @return "boo" if empty, "boo2" if test, "boo3" otherwise
     */
    private String helloWorld(String aString) {
        if (aString.equals("")) {
            return "boo";
        } else if (aString.equals("test")) {
            return "boo2";
        } else {
            return "boo3";
        }
    }

    /**
     * Does a for loop and then returns
     *
     * @return false
     */
    public boolean getABool() {
        // Comment hard to read or odd code
        for (int i = 0; i < 10; i++) {
            String test = "";
        }
        return false;
    }

    /**
     * Do nothing useful.
     *
     * @param input The input
     * @return Something
     */
    public static String doMagic(String input) {
        String output = "";
        switch (input) {
            case "a":
                output = "C";
                break;
            case "b":
                output = "horse";
                break;
            default:
                output = "none";
        }
        return output;
    }
}
