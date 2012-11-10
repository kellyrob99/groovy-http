package org.kar.http;

/**
 * Created with IntelliJ IDEA.
 * User: krobinson
 * Date: 12-11-07
 * Time: 10:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class HotDeployedClass
{
    private String name;
    private String other;

    @Override
    public String toString()
    {
        return other + ", " + name;
    }
}
