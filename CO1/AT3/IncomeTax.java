import java.util.*;
public class Main{
public static void main(String[]args){
Scanner sc=new Scanner(System.in);
int no=sc.nextInt();
sc.nextLine();
String name=sc.nextLine();
double s=sc.nextDouble(),t=0;
if(s<500000)t=0;
else if(s<=750000)t=s*0.05;
else if(s<=1000000)t=s*0.075;
else if(s<=1500000)t=s*0.10;
else if(s<=2000000)t=s*0.15;
else t=s*0.20;
System.out.println(t);
}
}