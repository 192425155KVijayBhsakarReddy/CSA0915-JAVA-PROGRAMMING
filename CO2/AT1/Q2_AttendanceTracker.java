import java.util.*;
public class Main{
    public static void main(String[]args){
        Scanner sc=new Scanner(System.in);
        String s=sc.next();
        int p=0,a=0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)=='P')p++;
            else if(s.charAt(i)=='A')a++;
        }
        int per=(p*100)/(p+a);
        System.out.print("Present="+p+" Absent="+a+" Attendance="+per+"% "+(per<75?"Not Eligible":"Eligible"));
    }
}