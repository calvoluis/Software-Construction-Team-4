import java.util.Scanner;

public class OverlappingRectangles {

    public static void main(String[] args) {

        System.out.println("Please put in coordinates for rectangles");
        System.out.println("(x1 y1 x2 y2 x3 y3 x4 y4)");

        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        in.close();
        input = input.trim();
        String[] inputArray = input.split(" ");
        if(inputArray.length != 8 && true){
            System.out.println("Invalid inputs please try again");
        }
        else{
            int[] coordinates = new int[8];
            for(int i=0; i<8; i++){
                System.out.println(i+" ");
            }
            String overlap = checkOverlap(coordinates[0],coordinates[1],coordinates[2],coordinates[3],coordinates[4],coordinates[5],coordinates[6],coordinates[7]);
            System.out.println(overlap);
        }
    }
}
