import java.util.*;
class Main{
    public static void main(String[] args){
        Scanner s=new Scanner(System.in);
        int m=s.nextInt();
        int n=s.nextInt();
        int k=s.nextInt();
        for(int i=m;i<=n;i=i+k+1)
            System.out.print(i+" ");
    }
}