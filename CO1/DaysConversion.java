import java.util.*;
public class Main{
public static void main(String[]args){
Scanner sc=new Scanner(System.in);
int d=sc.nextInt();
int y=d/365;d%=365;
int m=d/30;d%=30;
int w=d/7;d%=7;
System.out.println(y);
System.out.println(m);
System.out.println(w);
System.out.println(d);
}
}