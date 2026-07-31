import java.util.*;
class Main{
    public static void main(String[] args){
        Scanner s=new Scanner(System.in);
        int n=s.nextInt();
        int arr[]=new int[n];
        int count=0;
        for(int i=0;i<n;i++) arr[i]=s.nextInt();
        for(int i=0;i<n;i++){
            int c=0;
            for(int j=1;j<=arr[i];j++){
                if(arr[i]%j==0) c++;
            }
            if(c>2) count++;
        }
        System.out.println("Number of Composite Numbers = "+count);
    }
}