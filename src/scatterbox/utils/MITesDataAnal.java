package scatterbox.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.StringTokenizer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MITesDataAnal {

	// decoding states
	private static final int DIFFTIME = 0;
	private static final int UNIXTIME = 1;
	private static final int CHANNELID = 2;
	private static final int BYTE1 = 3;
	private static final int BYTE2 = 4;
	private static final int BYTE3 = 5;
	private static final int BYTE4 = 6;
	private static final int SYNCCHECK = 7;
	private static final int ONBODY_PAYLOAD = 8;
	private static final int OTHERMITE_PAYLOAD = 9;

	private FileInputStream inFile = null;
	private int decodingState;

	byte[] unixTime = new byte[6]; // stores one sensor packet
	byte[] syncData = new byte[5]; // rest of sync data
	long time; // stores absolute or relative time
	byte[] payload = new byte[4]; // payload
	byte[] data = new byte[1]; // to read data as bytes
	int millis; // milliseconds time
	short channelID;

	short byte1, byte2, byte3, byte4;
	// use to show timezone SimpleDateFormat dateFormat = new
	// SimpleDateFormat("MM/dd/yyyy  HH:mm:ss z");
	private static DateTimeFormatter my_dateFormat = DateTimeFormat.forPattern("MM/dd/yyyy  HH:mm:ss");
	private static DateTime dt;
	DateTimeZone my_timeZone = DateTimeZone.forOffsetHours(-4);
	long timeCorrection = 4 * 60 * 60 * 1000; // need correction as suspect
												// timezone is wrong on placelab
												// pcs
	long lasttimestamp = 0;
	boolean verbose = true;
	boolean superVerbose = false;
	MITesDataAnal(String fileName) throws Exception {
		// set up input file stream
		try {
			inFile = new FileInputStream((String) fileName);
		} catch (Exception e) {
			System.err.println("Error creating file stream " + e.getMessage());
			throw e;
		}
		decodingState = DIFFTIME;
		readFileStarttime(fileName);
	}
	/**
	 * 
	 * @param s
	 */
	private void readFileStarttime(String s)
	{
		StringTokenizer st = new StringTokenizer(s, ".");
		st.nextToken();
		String t = st.nextToken();
		if (superVerbose)
			System.out.println("filetime: " + t);
		st = new StringTokenizer(t, "-");
		int year = Integer.parseInt(st.nextToken());
		int month = Integer.parseInt(st.nextToken()) - 1; // JAN=0, DEC=11
		int day = Integer.parseInt(st.nextToken());
		int hr = Integer.parseInt(st.nextToken());
		int min = Integer.parseInt(st.nextToken());
		int sec = Integer.parseInt(st.nextToken());
		int mil = (int) Math.ceil(Float.parseFloat(st.nextToken()));
		// converting from date to unix time
		DateTime startTime = new
		DateTime(year, month, day, hr, min, sec, mil);
		this.lasttimestamp = startTime.getMillis();
		dt = new DateTime(this.lasttimestamp, my_timeZone);
		if (superVerbose)
			System.out.println("time updated to: "
					+ dt);
	}
	/**
	 * process binary sensor file.
	 * @param output the decoded file name.
	 * @param timeLimit the starting time for each binary sensor file.
	 */
	public void processFile(final String output, long timeLimit) {
		int val;
		int bytesRead = 0;
		byte[] data = new byte[1];
		Boolean sync = Boolean.FALSE;
		try {
			FileWriter fw = new FileWriter(output);
			while (inFile.available() != 0) {
				inFile.read(data, 0, 1);
				bytesRead++;
				switch (decodingState) {
				case DIFFTIME:
				{
					if (superVerbose)
						System.out.print("Diff time ");
					val = data[0] & 0xff; // converting from signed to unsigned
											// int
					// System.out.println("code: "+val);
					// DECODING THE TIME OF THE EVENT
					// if the absolute time needs to be read
					if (val == 255) {
						decodingState = UNIXTIME;
						bytesRead = 0;
					} else {
						time = val;
						time += lasttimestamp; // adjust time by adding previous
												// time
						lasttimestamp = time;
						long nrmillis = time % 1000;
						if (verbose)
						{
							// System.out.println("diff time: " + val + " ");
							// System.out.println("Time: "+ time + " " +
							// dateFormat.format(new java.util.Date(time)) + "."
							// + nrmillis);
							if (time <= timeLimit) {
								dt = new DateTime(time, my_timeZone);
								fw.write("Time: "
										+ time
										+ " "
										+ dt + "." + nrmillis);
							} else {
								dt = new DateTime(time, my_timeZone);
								fw.write("Time: "
										+ (time)
										+ " "
										+ dt + "."
										+ nrmillis);
							}
						}
						decodingState = CHANNELID;
						bytesRead = 0;
					}
					break;
				}
				case UNIXTIME:
					if (superVerbose)
						System.out.print("Unix time ");
					unixTime[bytesRead - 1] = data[0];
					if (bytesRead == 6) {
						// we have everything
						// check for a sync
						if ((unixTime[0] & 0xff) == 126) {
							// might be a sync
							sync = Boolean.TRUE;
							for (int i = 1; i < unixTime.length; i++) {
								if ((unixTime[i] & 0xff) != 126) {
									sync = Boolean.FALSE;
									if (superVerbose)
										System.out.println("unix time no sync"
												+ i + " ");
								} else {
									if (superVerbose)
										System.out.println("unix time sync "
												+ i + " ");
								}
							}
						}
						if (sync == Boolean.TRUE) {
							// need to look at more data to see if we are really
							// syncing
							decodingState = SYNCCHECK;
							bytesRead = 0;
						} else {
							// we aren't synching so go ahead
							time = unixTime[0] & 0xff;
							time = time | ((unixTime[1] & 0xff) << 8);
							time = time | ((unixTime[2] & 0xff) << 16);
							time = time | ((unixTime[3] & 0xff) << 24);
							time *= 1000;
							millis = unixTime[4] & 0xff;
							;
							millis = millis | ((unixTime[5] & 0xff) << 8);
							time += millis;
							time += timeCorrection;
							// converting C# to java milliseconds time
							// time+=+CSHARPTIMEOFFSET; ?? don't know what this
							// is but I try to handle it with timeCorrection
							long nrmillis = time % 1000;
							lasttimestamp = time;
							if (verbose)
							{
								// System.out.print("Time: "+ time + " " +
								// dateFormat.format(new java.util.Date(time)) +
								// "." + nrmillis);
								if (time <= timeLimit) {
									dt = new DateTime(time, my_timeZone);
									fw.write("Time: "
											+ time
											+ " "
											+ dt + "."
											+ nrmillis);

								} else {
									dt = new DateTime(time, my_timeZone);
									fw.write("Time: "
													+ (time)
													+ " "
													+ dt
													+ "." + nrmillis);
								}
							}
							// ready for next state
							decodingState = CHANNELID;
							bytesRead = 0;
						}
					}
					break;
				case SYNCCHECK:
					syncData[bytesRead - 1] = data[0];
					if (bytesRead == syncData.length) {
						// check for a sync
						sync = Boolean.TRUE;
						for (int i = 0; i < syncData.length; i++) {
							if ((syncData[i] & 0xff) != 126) {
								sync = Boolean.FALSE;
								if (superVerbose)
									System.out.println("sync check, no sync "
											+ i + " ");
							} else {
								if (superVerbose)
									System.out.println("sync " + i + " ");
							}
						}
						if (sync == Boolean.TRUE) {
							if (superVerbose)
								System.out.println("Saw sync packet");
						}
						bytesRead = 0;
						decodingState = DIFFTIME; // we're could be out of sync
													// but try your best
						sync = Boolean.FALSE; // reset sync flag (it was really
												// only for reporting)
					}
					break;
				case CHANNELID:
					// reading the channel ID
					if (superVerbose)
						System.out.print("Channel ID ");
					this.channelID = (short) (data[0] & 0xff);
					// if(verbose) System.out.print(" Channel: " +
					// this.channelID);
					if (verbose)
						fw.write(" Channel: " + this.channelID);
					if (channelID == 0) {
						decodingState = OTHERMITE_PAYLOAD;
					} else {
						decodingState = ONBODY_PAYLOAD;
					}
					bytesRead = 0;
					// decodingState = BYTE1;
					break;
				case ONBODY_PAYLOAD:
					if (superVerbose)
						System.out.print("Onbody Payload ");
					payload[bytesRead - 1] = data[0];
					if (bytesRead == payload.length) {
						int xAccel = ((payload[3] & 0xc0) << 2)
								| (payload[0] & 0xff);
						int yAccel = ((payload[3] & 0x30) << 4)
								| (payload[1] & 0xff);
						int zAccel = ((payload[3] & 0x0c) << 6)
								| (payload[2] & 0xff);
						// if ( verbose ) System.out.print( " x: " + xAccel +
						// " y: " + yAccel + " z: " + zAccel + "\n");
						if (verbose)
							fw.write(" x: " + xAccel + " y: " + yAccel + " z: "
									+ zAccel + "\n");
						if (superVerbose)
							System.out.print((short) (payload[0] & 0xff) + " "
									+ (short) (payload[1] & 0xff) + " "
									+ (short) (payload[2] & 0xff) + " "
									+ (short) (payload[3] & 0xff) + "\n");
						decodingState = DIFFTIME;
						bytesRead = 0;
					}
					break;
				case OTHERMITE_PAYLOAD:
					if (superVerbose)
						System.out.print("Othermite Payload ");
					payload[bytesRead - 1] = data[0];
					if (bytesRead == payload.length) {
						val = payload[0] & 0xff;
						int boardID = (val << 8);
						val = (payload[1] & 0xff);
						boardID = (boardID | val);
						val = payload[2] & 0xff;
						int attenuation = (short) (val << 8);
						val = payload[3] & 0xff;
						attenuation = (short) (attenuation | val);
						short sensorID = (short) (boardID >> 4);
						// int sensorID = ( (payload[0] & 0xff) << 4) | (
						// (payload[1] & 0xf0) >> 4 );
						// int sensorType = payload[1] & 0x0f;
						short sensorType = (short) (boardID & 0x0f);
						// int sensorData = ( (payload[2] & 0xff) << 3 ) |
						// ((payload[3] & 0xe0 ) >> 5);

						short sensorData = (short) (attenuation >> 5);
						// if ( verbose) System.out.print( " ID: " + sensorID +
						// " Type: " + sensorType + " Data: " + sensorData +
						// "\n");
						if (verbose)
							fw.write(" ID: " + sensorID + " Type: "
									+ sensorType + " Data: " + sensorData
									+ "\n");
						// if(superVerbose) System.out.print( (short)
						// (payload[0] & 0xff) + " " + (short) (payload[1] &
						// 0xff) + " " + (short) (payload[2] & 0xff) + " " +
						// (short) (payload[3] & 0xff) + "\n");
						if (superVerbose)
							fw.write((short) (payload[0] & 0xff) + " "
									+ (short) (payload[1] & 0xff) + " "
									+ (short) (payload[2] & 0xff) + " "
									+ (short) (payload[3] & 0xff) + "\n");
						decodingState = DIFFTIME;
						bytesRead = 0;
					}
					break;
				case BYTE1:
					this.byte1 = (short) (data[0] & 0xff);
					if (verbose)
						System.out.print(" Byte1: " + this.byte1);
					decodingState = BYTE2;
					break;
				case BYTE2:
					this.byte2 = (short) (data[0] & 0xff);
					if (verbose)
						System.out.print(" Byte2: " + this.byte2);
					decodingState = BYTE3;
					break;
				case BYTE3:
					this.byte3 = (short) (data[0] & 0xff);
					if (verbose)
						System.out.print(" Byte3: " + this.byte3);
					decodingState = BYTE4;
					break;
				case BYTE4:
					this.byte4 = (short) (data[0] & 0xff);
					if (verbose)
						System.out.println(" Byte4: " + this.byte4);
					decodingState = DIFFTIME;
					break;
				default:
					System.err.println("Error - invalid decoding state "
							+ decodingState);
					System.exit(1);
				}
			}
			fw.close();
		} catch (Exception e) {
			System.err.println("Error in reading" + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * 
	 * translate all the MITes sensor data (*.b files) into *.txt files.
	 * The index of files reflect the time sequence.
	 * @param timeLimit starting time for each sensor file.
	 * @param mitesFile the binary sensor file name.
	 * @param mites_decoded the decoded sensor file name.
	 * 
	 */

	public static void translate(long timeLimit, String mitesFile,
			String mites_decoded) {
		try {
			MITesDataAnal anal = new MITesDataAnal(mitesFile);
			anal.processFile(mites_decoded, timeLimit);
		} catch (Exception e) {
			System.out.println("wrong: " + e.getLocalizedMessage());
			System.exit(1);
		}
	}

	public static void translateAll(long timeLimit, String address,
			String decodedDir) {
		File directory = new File(address);
		String[] files = directory.list();
		int index = 0;
		for (int i = 0; i < files.length; i++) {
			String filename = address + files[i];
			String decodedName = decodedDir + "MITes_" + (index++) + ".txt";
			if (filename.contains("MITesCompositeBytes")) {
				System.out.println(filename);
				translate(timeLimit, filename, decodedName);
				timeLimit += 3600 * 1000;
			}
		}
	}

	public static void main(String[] args)	{
		String time = "08/23/2006  20:00:00";
		long timeLimit = my_dateFormat.parseDateTime(time).getMillis();
		try {
			MITesDataAnal anal = new MITesDataAnal(
					"/Users/seamusknox/Documents/datasets/placelab/2006-09-14/MITesNonAccelCompositeBytes.2006-9-14-0-0-0-31.b");
			anal.processFile("/Users/seamusknox/Documents/datasets/placelab/2006-09-14/MITesCompositeBytes.2006-9-14-0-0-0-31.decoded", timeLimit);
		} catch (Exception e) {
			System.out.println("wrong: " + e.getLocalizedMessage());
			System.exit(1);
		}
	}

}
