import java.util.*;
public class Main{
    public static void main(String[]args){
        Scanner sc=new Scanner(System.in);
        long bal=0;
        int dep=0,wit=0;
        while(sc.hasNextInt()){
            int v=sc.nextInt();
            bal+=v;
            if(v>0)dep++;
            else if(v<0)wit++;
        }
        System.out.print("Balance="+bal+" Deposits="+dep+" Withdrawals="+wit);
    }
}