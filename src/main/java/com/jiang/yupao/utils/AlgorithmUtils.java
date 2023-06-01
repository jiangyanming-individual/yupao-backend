package com.jiang.yupao.utils;
import java.util.List;


/**
 * 编辑距离算法，其实就是动态规划的问题：
 */
public class AlgorithmUtils {


    /**
     * 计算word1转为word2所需要的最少的次数
     * @param word1
     * @param word2
     * @return
     */
    public static int minDistance(String word1,String word2){

        int n= word1.length();
        int m=word2.length();

        if (m * n ==0){
            return m +n;
        }

        int[][] dp=new int[n +1][m+1];

        for (int i=0;i<n+1;i++){
            dp[i][0]=i;
        }

        for (int j=0;j<m+1;j++){
            dp[0][j]=j;
        }

        for (int i=1;i<=n;i++){
            for (int j=1;j<=m;j++){
                if (word1.charAt(i-1) == word2.charAt(j-1)){
                    dp[i][j]=dp[i-1][j-1];
                }else {
                    //替换， word1删除1 ，word2删除1；
                    dp[i][j]=Math.min(dp[i-1][j-1]+1,Math.min(dp[i-1][j]+1,dp[i][j-1]+1));
                }
            }
        }
        return dp[n][m];
    }

    /**
     * 方法重载的方法：
     * 用于计算两个tagList
     * @param tagList1
     * @param tagList2
     * @return
     */
    public static int minDistance(List<String> tagList1,List<String> tagList2){

        int n=tagList1.size();
        int m=tagList2.size();

        if (m * n == 0){
            return m + n;
        }
        int[][] dp=new int[n+1][m+1];

        //初始化：
        for(int i=0;i< n+1;i++){
            dp[i][0]=i;
        }
        for (int j=0;j< m+1;j++){
            dp[0][j]=j;
        }

        for (int i=1;i<=n;i++){
            for (int j=1;j<=m;j++){
                if (tagList1.get(i-1).equals(tagList2.get(j-1))){
                    dp[i][j]=dp[i-1][j-1];
                }else{
                    dp[i][j]=Math.min(dp[i-1][j-1]+1,Math.min(dp[i-1][j]+1,dp[i][j-1]+1));
                }
            }
        }
        return dp[n][m];
    }

}
