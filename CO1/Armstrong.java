import java.util.*;
public class Main{
public static void main(String[]args){
Scanner sc=new Scanner(System.in);
int n=sc.nextInt(),t=n,sum=0;
while(t>0){int d=t%10;sum+=d*d*d;t/=10;}
System.out.println(sum==n?"Armstrong Number":"Not an Armstrong Number");
}
}