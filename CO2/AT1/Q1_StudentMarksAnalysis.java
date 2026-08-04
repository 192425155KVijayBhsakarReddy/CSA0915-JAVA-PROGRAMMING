import java.util.*;
public class Main{
    public static void main(String[]args){
        Scanner sc=new Scanner(System.in);
        int[]marks=new int[10];
        int highest=0,sum=0;
        for(int i=0;i<10;i++){
            marks[i]=sc.nextInt();
            if(i==0||marks[i]>highest)highest=marks[i];
            sum+=marks[i];
        }
        double avg=(double)sum/10;
        int count=0;
        for(int x:marks)if(x>avg)count++;
        System.out.println("Highest = "+highest);
        System.out.printf("Average = %.1f%n",avg);
        System.out.println("Students Above Average = "+count);
        sc.close();
    }
}