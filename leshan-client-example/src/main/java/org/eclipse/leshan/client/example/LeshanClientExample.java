/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Zebra Technologies - initial API and implementation
 *     Sierra Wireless, - initial API and implementation
 *     Bosch Software Innovations GmbH, - initial API and implementation
 *******************************************************************************/

package org.eclipse.leshan.client.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.resource.ObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.DeregisterRequest;
import org.eclipse.leshan.core.request.RegisterRequest;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.RegisterResponse;
import org.eclipse.leshan.core.response.WriteResponse;

/*
 * To build: 
 * mvn assembly:assembly -DdescriptorId=jar-with-dependencies
 * To use:
 * java -jar target/leshan-client-*-SNAPSHOT-jar-with-dependencies.jar 127.0.0.1 5683
 */
public class LeshanClientExample {
    private String registrationID;
    private final Location locationInstance = new Location();
    private final parkingSpot parkingSpotInstance = new parkingSpot();
    private final multipleAxisJoystick multipleAxisJoystickInstance = new multipleAxisJoystick();
    private final addressableTextDisplay addressableTextDisplayInstance = new addressableTextDisplay();
    private final firmwareUpdate firmwareUpdateInstance = new firmwareUpdate();

    public static void main(final String[] args) {
        if (args.length != 4 && args.length != 2) {
            System.out.println(
                    "Usage:\njava -jar target/leshan-client-example-*-SNAPSHOT-jar-with-dependencies.jar [ClientIP] [ClientPort] ServerIP ServerPort");
        } else {
            if (args.length == 4)
                new LeshanClientExample(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
            else
                new LeshanClientExample("0", 0, args[0], Integer.parseInt(args[1]));

        }

    }

    public LeshanClientExample(final String localHostName, final int localPort, final String serverHostName,
            final int serverPort) {

        // Initialize object list
        ObjectsInitializer initializer = new ObjectsInitializer();

        // initializer.setClassForObject(3, Device.class);
        List<ObjectEnabler> enablers = initializer.create();

        initializer.setInstancesForObject(32700, parkingSpotInstance);
        enablers.add(initializer.create(32700));

        initializer.setInstancesForObject(6, locationInstance);
        enablers.add(initializer.create(6));

        initializer.setInstancesForObject(3341, addressableTextDisplayInstance);
        enablers.add(initializer.create(3341));

        initializer.setInstancesForObject(3345, multipleAxisJoystickInstance);
        enablers.add(initializer.create(3345));

        initializer.setInstancesForObject(5, firmwareUpdateInstance);
        enablers.add(initializer.create(5));

        // Create client
        final InetSocketAddress clientAddress = new InetSocketAddress(localHostName, localPort);
        final InetSocketAddress serverAddress = new InetSocketAddress(serverHostName, serverPort);

        final LeshanClient client = new LeshanClient(clientAddress, serverAddress, enablers);

        // Start the client
        client.start();

        // Register to the server
        final String endpointIdentifier = "Parking-Spot-13-test";
        RegisterResponse response = client.send(new RegisterRequest(endpointIdentifier));
        if (response == null) {
            System.out.println("Registration request timeout");
            return;
        }

        System.out.println("Device Registration (Success? " + response.getCode() + ")");
        if (response.getCode() != ResponseCode.CREATED) {
            // TODO Should we have a error message on response ?
            // System.err.println("\tDevice Registration Error: " + response.getErrorMessage());
            System.err.println(
                    "If you're having issues connecting to the LWM2M endpoint, try using the DTLS port instead");
            return;
        }

        registrationID = response.getRegistrationID();
        System.out.println("\tDevice: Registered Client Location '" + registrationID + "'");

        String command = "python raspberrypijoystick.py &";
        String output = executeCommand(command);

        // Deregister on shutdown and stop client.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (registrationID != null) {
                    System.out.println("\tDevice: Deregistering Client '" + registrationID + "'");
                    client.send(new DeregisterRequest(registrationID), 1000);
                    client.stop();
                }
            }
        });

        /*
         * Change the location through the Console /Scanner scanner = new Scanner(System.in); System.out.println(
         * "Press 'w','a','s','d' to change reported Location."); while (scanner.hasNext()) { String nextMove =
         * scanner.next(); locationInstance.moveLocation(nextMove); } scanner.close();
         */
    }

    /*
     * public static class Device extends BaseInstanceEnabler {
     * 
     * public Device() { // notify new date each 5 second Timer timer = new Timer(); timer.schedule(new TimerTask() {
     * 
     * @Override public void run() { fireResourcesChange(13); } }, 5000, 5000); }
     * 
     * @Override public ReadResponse read(int resourceid) { System.out.println("Read on Device Resource " + resourceid);
     * switch (resourceid) { case 0: return ReadResponse.success(resourceid, getManufacturer()); case 1: return
     * ReadResponse.success(resourceid, getModelNumber()); case 2: return ReadResponse.success(resourceid,
     * getSerialNumber()); case 3: return ReadResponse.success(resourceid, getFirmwareVersion()); case 9: return
     * ReadResponse.success(resourceid, getBatteryLevel()); case 10: return ReadResponse.success(resourceid,
     * getMemoryFree()); case 11: Map<Integer, Long> errorCodes = new HashMap<>(); errorCodes.put(0, getErrorCode());
     * return ReadResponse.success(resourceid, errorCodes, Type.INTEGER); case 13: return
     * ReadResponse.success(resourceid, getCurrentTime()); case 14: return ReadResponse.success(resourceid,
     * getUtcOffset()); case 15: return ReadResponse.success(resourceid, getTimezone()); case 16: return
     * ReadResponse.success(resourceid, getSupportedBinding()); default: return super.read(resourceid); } }
     * 
     * @Override public ExecuteResponse execute(int resourceid, String params) { System.out.println(
     * "Execute on Device resource " + resourceid); if (params != null && params.length() != 0) System.out.println(
     * "\t params " + params); return ExecuteResponse.success(); }
     * 
     * @Override public WriteResponse write(int resourceid, LwM2mResource value) { System.out.println(
     * "Write on Device Resource " + resourceid + " value " + value); switch (resourceid) { case 13: return
     * WriteResponse.notFound(); case 14: setUtcOffset((String) value.getValue()); fireResourcesChange(resourceid);
     * return WriteResponse.success(); case 15: setTimezone((String) value.getValue()); fireResourcesChange(resourceid);
     * return WriteResponse.success(); default: return super.write(resourceid, value); } }
     * 
     * private String getManufacturer() { return "Leshan Example Device"; }
     * 
     * private String getModelNumber() { return "Model 500"; }
     * 
     * private String getSerialNumber() { return "LT-500-000-0001"; }
     * 
     * private String getFirmwareVersion() { return "1.0.0"; }
     * 
     * private long getErrorCode() { return 0; }
     * 
     * private int getBatteryLevel() { final Random rand = new Random(); return rand.nextInt(100); }
     * 
     * private int getMemoryFree() { final Random rand = new Random(); return rand.nextInt(50) + 114; }
     * 
     * private Date getCurrentTime() { return new Date(); }
     * 
     * private String utcOffset = new SimpleDateFormat("X").format(Calendar.getInstance().getTime());;
     * 
     * private String getUtcOffset() { return utcOffset; }
     * 
     * private void setUtcOffset(String t) { utcOffset = t; }
     * 
     * private String timeZone = TimeZone.getDefault().getID();
     * 
     * private String getTimezone() { return timeZone; }
     * 
     * private void setTimezone(String t) { timeZone = t; }
     * 
     * private String getSupportedBinding() { return "U"; } }
     */
    public static class Location extends BaseInstanceEnabler {
        private Random random;
        private float latitude;
        private float longitude;
        // private Date timestamp;

        public Location() {
            random = new Random();
            latitude = Float.valueOf(random.nextInt(180));
            longitude = Float.valueOf(random.nextInt(360));
            // timestamp = new Date();
        }

        @Override
        public ReadResponse read(int resourceid) {
            System.out.println("Read on Location Resource " + resourceid);
            switch (resourceid) {
            case 0:
                return ReadResponse.success(resourceid, getLatitude());
            case 1:
                return ReadResponse.success(resourceid, getLongitude());
            default:
                return super.read(resourceid);
            }
        }

        /*
         * public void moveLocation(String nextMove) { switch (nextMove.charAt(0)) { case 'w': moveLatitude(1.0f);
         * break; case 'a': moveLongitude(-1.0f); break; case 's': moveLatitude(-1.0f); break; case 'd':
         * moveLongitude(1.0f); break; } }
         * 
         * private void moveLatitude(float delta) { latitude = latitude + delta; timestamp = new Date();
         * fireResourcesChange(0, 5); }
         * 
         * private void moveLongitude(float delta) { longitude = longitude + delta; timestamp = new Date();
         * fireResourcesChange(1, 5); }
         */

        public String getLatitude() {
            return Float.toString(latitude - 90.0f);
        }

        public String getLongitude() {
            return Float.toString(longitude - 180.f);
        }

    }

    public static class addressableTextDisplay extends BaseInstanceEnabler {
        private String Text;

        public addressableTextDisplay() {
            Text = "green";
            String command = "python ParkingLot/ledDisplayGreen.py";
            String output = executeCommand(command);
        }

        @Override
        public ReadResponse read(int resourceid) {
            System.out.println("Read on addressableTextDisplay Resource " + resourceid);
            switch (resourceid) {
            case 5527:
                return ReadResponse.success(resourceid, getText());
            default:
                return super.read(resourceid);
            }
        }

        @Override
        public WriteResponse write(int resourceid, LwM2mResource value) {
            System.out.println("Write on Device Resource " + resourceid + " value " + value);
            switch (resourceid) {
            case 5527:
                String val = (String) value.getValue();
                switch (val.charAt(0)) {
                case 'o':
                    Text = "orange";
                    String command = "python ParkingLot/ledDisplayOrange.py";
                    String output = executeCommand(command);
                    return WriteResponse.success();
                case 'r':
                    Text = "red";
                    String command1 = "python ParkingLot/ledDisplayRed.py";
                    String output1 = executeCommand(command1);
                    return WriteResponse.success();
                case 'g':
                    Text = "green";
                    String command2 = "python ParkingLot/ledDisplayGreen.py";
                    String output2 = executeCommand(command2);
                    return WriteResponse.success();

                }
            default:
                return super.write(resourceid, value);
            }
        }

        public String getText() {
            return (Text);
        }

    }

    public static class multipleAxisJoystick extends BaseInstanceEnabler {
        private int DigitalInputCounter = 0;
        private float YValue = -100;

        public multipleAxisJoystick() {
            DigitalInputCounter = 0;
            YValue = -100;

        }

        @Override
        public ReadResponse read(int resourceid) {
            System.out.println("Read on multipleAxisJoystick Resource " + resourceid);
            switch (resourceid) {
            case 5501:
                return ReadResponse.success(resourceid, getDigitalInputCounter());
            case 5703:
                return ReadResponse.success(resourceid, getYValue());
            default:
                return super.read(resourceid);
            }
        }

        @Override
        public WriteResponse write(int resourceid, LwM2mResource value) {
            System.out.println("Write on Device Resource " + resourceid + " value " + value);
            switch (resourceid) {
            case 5703:
                String val = value.getValue().toString();
                System.out.println(val);
                switch (val.charAt(0)) {
                case '1':
                    if (YValue == -100) {
                        YValue = 100;
                        DigitalInputCounter += 1;
                        fireResourcesChange(5703);
                        return WriteResponse.success();
                    }

                case '-':
                    YValue = -100;
                    fireResourcesChange(5703);
                    return WriteResponse.success();

                }
            default:
                return super.write(resourceid, value);
            }
        }

        private int getDigitalInputCounter() {
            return (DigitalInputCounter);

        }

        private float getYValue() {
            return (YValue);

        }

    }

    public static class parkingSpot extends BaseInstanceEnabler {
        private String ParkingSpotID;
        private String ParkingSpotState;
        private String VehicleID;
        private float BillingRate;

        public parkingSpot() {
            ParkingSpotID = "Parking-Spot-13-test";
            ParkingSpotState = "free";
            VehicleID = "";
            BillingRate = 0.01f;
        }

        @Override
        public ReadResponse read(int resourceid) {
            System.out.println("Read on Location Resource " + resourceid);
            switch (resourceid) {
            case 32800:
                return ReadResponse.success(resourceid, getParkingSpotID());
            case 32801:
                return ReadResponse.success(resourceid, getParkingSpotState());
            case 32802:
                return ReadResponse.success(resourceid, getVehicleID());
            case 32803:
                return ReadResponse.success(resourceid, getBillingRate());
            default:
                return super.read(resourceid);
            }
        }

        @Override
        public WriteResponse write(int resourceid, LwM2mResource value) {
            System.out.println("Write on Device Resource " + resourceid + " value " + value);
            switch (resourceid) {
            case 32801:
                String val = (String) value.getValue();
                switch (val.charAt(0)) {
                case 'f':
                    ParkingSpotState = "free";
                    fireResourcesChange(32801);
                    return WriteResponse.success();

                case 'r':
                    ParkingSpotState = "reserved";
                    fireResourcesChange(32801);
                    Path text_path = Paths.get("statusOfJoystick.txt");
                    List<String> lines;
                    try {
                        lines = Files.readAllLines(text_path);
                        char a = lines.get(1).charAt(0);
                        while (!(a == ('o'))) {
                            lines = Files.readAllLines(text_path);
                            a = lines.get(1).charAt(0);
                            TimeUnit.SECONDS.sleep(1);
                        }
                        String command1 = "python ParkingLot/ledDisplayRed.py";
                        String output1 = executeCommand(command1);
                        ParkingSpotState = "occupied";
                        fireResourcesChange(32801);

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Path text_path1 = Paths.get("statusOfJoystick.txt");
                    List<String> lines1;
                    try {
                        lines1 = Files.readAllLines(text_path);
                        char a = lines1.get(1).charAt(0);
                        while (!(a == ('f'))) {
                            lines1 = Files.readAllLines(text_path);
                            a = lines1.get(1).charAt(0);
                            TimeUnit.SECONDS.sleep(1);
                        }
                        String command1 = "python ParkingLot/ledDisplayGreen.py";
                        String output1 = executeCommand(command1);
                        ParkingSpotState = "free";
                        fireResourcesChange(32801);

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    return WriteResponse.success();
                case 'o':
                    ParkingSpotState = "occupied";
                    fireResourcesChange(32801);
                    return WriteResponse.success();

                }
            case 32802:
                VehicleID = (String) value.getValue();
                return WriteResponse.success();
            case 32803:
                BillingRate = (float) value.getValue();
                return WriteResponse.success();
            default:
                return super.write(resourceid, value);
            }

        }

        private String getParkingSpotID() {
            return (ParkingSpotID);
        }

        private String getParkingSpotState() {
            return (ParkingSpotState);
        }

        private String getVehicleID() {
            return (VehicleID);
        }

        private float getBillingRate() {
            return (BillingRate);
        }

    }

    public static class firmwareUpdate extends BaseInstanceEnabler {
        private String PackageURI;

        public firmwareUpdate() {
            PackageURI = "";
        }

        /*
         * @Override public ReadResponse read(int resourceid) { System.out.println("Read on Location Resource " +
         * resourceid); switch (resourceid) { case 1: return ReadResponse.success(resourceid, execute(1,)) default:
         * return super.read(resourceid); } }
         */

        @Override
        public WriteResponse write(int resourceid, LwM2mResource value) {
            System.out.println("Write on Device Resource " + resourceid + " value " + value);
            switch (resourceid) {
            case 1:
                String val = (String) value.getValue();
                PackageURI = val;
                // execute(resourceid, val);
                return WriteResponse.success();

            default:
                return super.write(resourceid, value);
            }

        }

        @Override
        public ExecuteResponse execute(int resourceid, String params) {
            System.out.println("Execute on Device resource " + resourceid);

            String command = "rm -r ParkingLot";
            String output = executeCommand(command);
            String command1 = "git clone " + PackageURI;
            String output1 = executeCommand(command1);

            if (params != null && params.length() != 0)
                System.out.println("\t params " + params);
            return ExecuteResponse.success();
        }

    }

    private static String executeCommand(String command) {
        System.out.println(command);
        StringBuffer output = new StringBuffer();

        Process p;

        try {
            p = Runtime.getRuntime().exec(command);
            new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
            new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println(output.toString());

        return output.toString();

    }
}
