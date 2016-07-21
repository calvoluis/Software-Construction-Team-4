import java.util.Scanner;

public class OverlappingRectangles {

	public static void main(String[] args) {
		// added to run program without using JUnit
		System.out.println("Please put in coordinates for rectangles");
		System.out.println("(x1 y1 x2 y2 x3 y3 x4 y4)");

		Scanner in = new Scanner(System.in);
		String input = in.nextLine();
		in.close();
		input = input.trim();
		String[] inputArray = input.split(" ");
		if(inputArray.length != 8){System.out.println("Invalid inputs please try again");}
		else{
			int[] coordinates = new int[8];
			for(int i=0; i<8; i++){
				try{
					coordinates[i] = Integer.parseInt(inputArray[i]);
				}catch(NumberFormatException e){
					System.out.println("Invalid inputs please try again");
				}
			}
			String overlap = checkOverlap(coordinates[0],coordinates[1],coordinates[2],coordinates[3],coordinates[4],coordinates[5],coordinates[6],coordinates[7]);
			System.out.println(overlap);
		}
	}

	//added for test 1
	//checks to see if coordinates on same axis could be part of a rectangle
	public static boolean checkSameAxisCoordinates(int c1, int c2) {
		// set to false to satisfy test 1
		// set to condition to satisfy test 2
		return c1 < c2;
	}

	//added for test 3
	public static boolean checkRectangle(int x1, int y1, int x2, int y2) {
		// set to true to satisfy test 3
		//set to call checkSameAxisCoordinates() to satisfy test 4
		//set to call twice to satisfy test 5
		return checkSameAxisCoordinates(x1, x2) && checkSameAxisCoordinates(y1, y2);
	}

	//added for test 6
	public static boolean checkRectangles(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
		// set to true to satisfy test 6
		//set to call checkRectangle() to satisfy test 7
		//added additional call checkRectangle() to satisfy test 8
		return checkRectangle(x1, y1, x2, y2) && checkRectangle(x3, y3, x4, y4);
	}

	//added for test 9
	public static String checkOverlap(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
		if(checkRectangles(x1, y1, x2, y2, x3, y3, x4, y4)){
			//created string variable to keep track of which overlaps for test 14
			String overlaps = "";
			// set to false to satisfy test 9
			// set to check overlap scenario: x4,y4 within x1,y1 and x2,y2
			if((x1<x4 && x4<x2) && (y1<y4 && y4<y2))
				overlaps += "(x4,y4) ";
			//set to check for above scenario plus: x4,y3 within x1,y1 and x2,y2
			if((x1<x4 && x4<x2) && (y1<y3 && y3<y2))
				overlaps += "(x4,y3) ";
			//set to check for above scenarios plus: x3,y3 within x1,y1 and x2,y2
			if((x1<x3 && x3<x2) && (y1<y3 && y3<y2))
				overlaps += "(x3,y3) ";
			//set to check for above scenarios plus: x3,y4 within x1,y1 and x2,y2
			if((x1<x3 && x3<x2) && (y1<y4 && y4<y2))
				overlaps += "(x3,y4) ";

			//checks for no overlaps for test 14
			if(overlaps == "")
				return "No overlaps";
			else{
				return overlaps + "overlap";
			}
		}
		return "Not a rectangle";
	}

	public static void switchStatementTest(int a){
		int monthNumber = 0;

        if (month == null) {
            return monthNumber;
        }

        switch (month.toLowerCase()) {
            case "january":
                monthNumber = 1;
                break;
            case "february":
                monthNumber = 2;
                break;
            case "march":
                monthNumber = 3;
                break;
            case "april":
                monthNumber = 4;
                break;
            case "may":
                monthNumber = 5;
                break;
            case "june":
                monthNumber = 6;
                break;
            case "july":
                monthNumber = 7;
                break;
            case "august":
                monthNumber = 8;
                break;
            case "september":
                monthNumber = 9;
                break;
            case "october":
                monthNumber = 10;
                break;
            case "november":
                monthNumber = 11;
                break;
            case "december":
                monthNumber = 12;
                break;
            default:
                monthNumber = 0;
                break;
        }

        return monthNumber;
	}

	public void writeList() {
	    PrintWriter out = null;

	    try {
	        System.out.println("Entering" + " try statement");

	        out = new PrintWriter(new FileWriter("OutFile.txt"));
	        for (int i = 0; i < SIZE; i++) {
	            out.println("Value at: " + i + " = " + list.get(i));
	        }
	    } catch (IndexOutOfBoundsException e) {
	        System.err.println("Caught IndexOutOfBoundsException: "
	                           +  e.getMessage());

	    } catch (IOException e) {
	        System.err.println("Caught IOException: " +  e.getMessage());

	    } finally {
	        if (out != null) {
	            System.out.println("Closing PrintWriter");
	            out.close();
	        }
	        else {
	            System.out.println("PrintWriter not open");
	        }
	    }
	}
}
