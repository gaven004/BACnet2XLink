package com.g.bacnet2xlink;

public class Template {
    public static void main(String[] args) {
        for (int i = 3000040; i <= 3000081; i++) {
            System.out.println(String.format("obj = new BACnetObject(device, ObjectType.forName(\"binary-value\"), %d, \"elevator-%d\");", i, i));
            System.out.println("obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);");
            System.out.println("device.addObject(obj);");
            System.out.println("");
        }
    }
}
