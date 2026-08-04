import java.util.*;
public class Main{
    public static void main(String[]args){
        Scanner sc=new Scanner(System.in);
        int n=sc.nextInt(),pass=0,fail=0;
        char[]g=new char[n];
        for(int i=0;i<n;i++){
            int m=sc.nextInt();
            if(m>=50)pass++;else fail++;
            if(m>=90)g[i]='A';
            else if(m>=75)g[i]='B';
            else if(m>=60)g[i]='C';
            else g[i]='F';
        }
        System.out.print("Pass="+pass+" Fail="+fail);
    }
}