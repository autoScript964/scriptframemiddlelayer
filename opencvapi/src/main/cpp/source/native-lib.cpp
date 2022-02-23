//
// Created by Administrator on 2019-10-16.
//
#include "opencv2/opencv.hpp"
#include "opencv2/imgcodecs.hpp"
#include <jni.h>
#include <iostream>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstring>
#include <string>
#include <string.h>
#include <pthread.h>
#include <iostream>
#include <vector>
#include <algorithm>
#include <thread>
#include <mutex>

using namespace cv;

#define  LOG_TAG    "native-dev"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
using namespace std;



#ifdef __cplusplus
extern "C" {
#endif



mutex my_lock;
bool check_sub;
void testthread(int i, Mat mGr, uchar *d, int row, int col, int *str_start_rgb,
                vector<vector<int> > str_sub_rgb, int rect_w, int rect_y);
void similar_arithmetic(Mat mGr, uchar *d, int row, int col, int *str_start_rgb,
                        vector<vector<int> > str_sub_rgb, vector<vector<int> > str_sub_xy,
                        int rect_w, int rect_y, int *range_, double sim, int *return_value);
void similar_arithmetic_1(Mat mGr, uchar *d, int row, int col, int *str_start_rgb,
                          vector<vector<int> > str_sub_rgb, vector<vector<int> > str_sub_xy,
                          int rect_w, int rect_y, int *range_, double sim, int *return_value);
void sub_similar_arithmetic(int i, int j, vector<vector<int> > str_sub_xy,
                            vector<vector<int> > str_sub_rgb, int rect_y, int rect_w, Mat mGr,
                            int threshold, bool *check, int k);
void calculateSimilarity(Vec3b rgb1, int *rgb2);
vector<string> split(const string &, const string &);
#ifdef __cplusplus
}
#endif







vector<string> split(const string &str, const string &delim) {
    vector<string> res;
    if ("" == str) return res;
    //先将要切割的字符串从string类型转换为char*类型
    char *strs = new char[str.length() + 1]; //不要忘了
    strcpy(strs, str.c_str());

    char *d = new char[delim.length() + 1];
    strcpy(d, delim.c_str());

    char *p = strtok(strs, d);
    while (p) {
        string s = p; //分割得到的字符串转换为string类型
        res.push_back(s); //存入结果数组
        p = strtok(NULL, d);
    }

    return res;
}

void calculateSimilarity(Vec3b rgb1, int *rgb2) {
    double trageSim = 0.9;
    double c = 25.5;
    int c1 = rgb1[0] - rgb2[0];
//    if (c1>c){
//        return;
//    }
//    int c2=abs(rgb1[1]-rgb2[1]);
//    if(c2>c){
//        return;
//    }
//    int c3=abs(rgb1[2]-rgb2[2]);
//    if(c3>c){
//        return;
//    }



//    double sim__ =1-(c1+c2+c3)/765;
//    double sim__ =765-(c1+c2+c3);

/*
    int rgb1_rgb2_1=abs(rgb1[0]-rgb2[0]);
    if (rgb1_rgb2_1>c){
        return;
    }

//    double sim1=fabs((rgb1[0]/255) - (rgb2[0]/255));

    double sim1=rgb1_rgb2_1/255;
//    if (sim1/3>c){
//        return;
//    }
//    double sim2=fabs((rgb1[1]/255) - (rgb2[1]/255));

    int rgb1_rgb2_2=abs(rgb1[1]-rgb2[1]);
//    if (rgb1_rgb2_2>c){
//        return;
//    }
    double sim2=rgb1_rgb2_2/255;

//    if((sim1+sim2)/3>c){
//        return;
//    }
//    double sim3=fabs((rgb1[2]/255) - (rgb2[2]/255));

    double sim3=fabs((rgb1[2]-rgb2[2])/255);
    double sim = 1-((sim1+sim2+sim3)/3);

//    LOGI("======multipointFindColor====================================================================%d%d%d",rgb2[0],rgb2[1],rgb2[2] );
//    LOGI("======multipointFindColor====================================================================%d%d%d" ,rgb1[0],rgb1[1],rgb1[2]);
//    double sim =0.0;
//    for (int i = 0; i <3 ; ++i) {
////      sim=sim+pow(pow((int)rgb1[i]/255 -  (int)rgb2[i]/255,2),0.5);
//        sim=sim+fabs((rgb1[i]/255) - (rgb2[i]/255));
//    }

//    if (sim>trageSim){
//        LOGI("======multipointFindColor===================================================================sim=%lf" ,sim);
//        LOGI("======multipointFindColor===================================================================sim1=%lf" ,sim1);
//        LOGI("======multipointFindColor===================================================================sim2=%lf" ,sim2);
//        LOGI("======multipointFindColor===================================================================sim3=%lf" ,sim3);
//    }
*/
}

int myAdd(int num1, int num2) {
    if (num2 == 0) return num1;
    int sum = 0, carry = 0;
    sum = num1 ^ num2;    // 按位抑或
    carry = (num1 & num2) << 1;
    return myAdd(sum, carry);
}


int myMinus(int num1, int num2) {
    return myAdd(num1, myAdd(~num2, 1));
}






void testthread(int i, Mat mGr, uchar *d, int row, int col, int *str_start_rgb,
                vector<vector<int> > str_sub_rgb, int rect_w, int rect_y) {
    LOGI("======mtestthread============================= ");

    LOGI("======mtestthread============================= 1");
    LOGI("======multipointFindColor==========================return============================= %d ;%d;%d",
         str_sub_rgb[0][0], str_sub_rgb[0][1], str_sub_rgb[0][2]);
}


void similar_arithmetic(Mat mGr, uchar *d, int row, int col, int *str_start_rgb,
                        vector<vector<int> > str_sub_rgb, vector<vector<int> > str_sub_xy,
                        int rect_w, int rect_y, int *range_, double sim, int *return_value) {
    double target = sim;
    int c1, c2, c3, c4, c12;
    int index;
    int threshold = (int) (765 * (1 - target));
    int up_sub[3] = {-1, -1, -1};
    int up_sub1[3] = {-1, -1, -1};
    vector<vector<int> > str_sub_xy_copy(str_sub_xy);
    vector<vector<int> > str_sub_rgb_copy(str_sub_rgb);
//    memcpy(str_sub_xy_copy, str_sub_xy, str_sub_xy.size()*2*4);

//    for (int i =0;i<str_sub_xy_copy.size();i++){
//        LOGI("======multipointFindColor=========================== %d ; %d ",str_sub_xy_copy[i][0],str_sub_xy[i][0]);
//    }



    for (int i = range_[1]; i < range_[3]; ++i) {
        d = mGr.ptr<uchar>(i);
        for (int j = range_[0]; j < range_[2]; ++j) {
            if (return_value[0] > -1) {
                return;
            }
            index = j * 3;
            int r1, g1, b1;
            r1 = d[index];
            g1 = d[index + 1];
            b1 = d[index + 2];
            if (up_sub[0] == r1 && up_sub[1] == g1 && up_sub[2] == b1) {
                continue;
            }
            if (r1 > str_start_rgb[0]) {
                c1 = r1 - str_start_rgb[0];
            } else {
                c1 = str_start_rgb[0] - r1;
            }
//            c1 = (c1 ^ (c1 >> 31)) - (c1 >> 31);
//            if (c1 > threshold) { continue; }
            if (g1 > str_start_rgb[1]) {
                c2 = g1 - str_start_rgb[1];
            } else {
                c2 = str_start_rgb[1] - g1;
            }
//            c2 = (c2 ^ (c2 >> 31)) - (c2 >> 31);

            c12 = c1 + c2;
//            if (c12 > threshold) { continue; }
            if (b1 > str_start_rgb[2]) {
                c3 = b1 - str_start_rgb[2];
            } else {
                c3 = str_start_rgb[2] - b1;
            }
//            c3 = (c3 ^ (c3 >> 31)) - (c3 >> 31);//绝对值
            c4 = c12 + c3;
            if (c4 < threshold) {
                up_sub[0] = -1;
                up_sub[1] = -1;
                up_sub[2] = -1;
                int x = j;
                int y = i;
                bool check = true;
                for (size_t k = 0; k < str_sub_xy.size(); ++k) {
                    int row = i + str_sub_xy[k][1];
                    int col = j + str_sub_xy[k][0];
                    if (row < 0 || row > rect_y || col < 0 || col > rect_w) {
                        check = false;
                        break;
                    }
                    index = col * 3;
                    r1 = mGr.ptr<uchar>(row)[index];
                    g1 = mGr.ptr<uchar>(row)[index + 1];
                    b1 = mGr.ptr<uchar>(row)[index + 2];
//                    if (up_sub1[0] == r1 && up_sub1[1] == g1 && up_sub1[2] == b1) {
//                        check = false;
//                        break;
//                    }
                    if (r1 > str_sub_rgb[k][0]) {
                        c1 = r1 - str_sub_rgb[k][0];
                    } else {
                        c1 = str_sub_rgb[k][0] - r1;
                    }
//                    if(c1 < 0){
//                        c1 = (c1 ^ (c1 >> 31)) - (c1 >> 31);
//                    }
                    if (c1 > threshold) {
                        check = false;
                        break;
                    }
                    c2 = g1 - str_sub_rgb[k][1];
                    c2 = (c2 ^ (c2 >> 31)) - (c2 >> 31);
                    c12 = c1 + c2;
                    if (c12 > threshold) {
                        check = false;
                        break;
                    }
                    c3 = b1 - str_sub_rgb[k][2];
                    c3 = (c3 ^ (c3 >> 31)) - (c3 >> 31);
                    c4 = c12 + c3;
                    if (c4 > threshold) {
//                        if (k > 0) {
//                            str_sub_xy[0][0] = str_sub_xy_copy[k][0];
//                            str_sub_xy[0][1] = str_sub_xy_copy[k][1];
//                            str_sub_xy[k][0] = str_sub_xy_copy[0][0];
//                            str_sub_xy[k][1] = str_sub_xy_copy[0][1];
//                            str_sub_xy_copy.assign(str_sub_xy.begin(), str_sub_xy.end());
//                            str_sub_rgb[0][0] = str_sub_rgb_copy[k][0];
//                            str_sub_rgb[0][1] = str_sub_rgb_copy[k][1];
//                            str_sub_rgb[0][2] = str_sub_rgb_copy[k][2];
//                            str_sub_rgb[k][0] = str_sub_rgb_copy[0][0];
//                            str_sub_rgb[k][1] = str_sub_rgb_copy[0][1];
//                            str_sub_rgb[k][2] = str_sub_rgb_copy[0][2];
//                            str_sub_rgb_copy.assign(str_sub_rgb.begin(), str_sub_rgb.end());
//                        }
//                        up_sub1[0] = r1;
//                        up_sub1[1] = g1;
//                        up_sub1[2] = b1;
                        check = false;
                        break;
                    }
//                    else {
//                        up_sub1[0] = -1;
//                        up_sub1[1] = -1;
//                        up_sub1[2] = -1;
//                    }

                }
                /*
                thread threads[str_sub_xy.size()];
                for (size_t k = 0; k < str_sub_xy.size(); k++) {
                    threads[k] = thread(sub_similar_arithmetic, i, j, str_sub_xy, str_sub_rgb,
                                        rect_y, rect_w, mGr, threshold, &check, k);
                }
                for (size_t k = 0; k < str_sub_xy.size(); k++) {
                    threads[k].join();
                }
                */

                if (check) {
//                    LOGI("======multipointFindColor==========================lock==============================");
                    my_lock.lock();
                    if (return_value[0] > -1) {
//                        LOGI("======multipointFindColor==========================return==============================");
                        my_lock.unlock();
                        return;
                    }
//                    LOGI("======multipointFindColor==========================return==============================%lf ;%d ;%d ",sim, x, y);
                    return_value[0] = x;
                    return_value[1] = y;
//                    LOGI("======multipointFindColor==========================unlock==============================");
                    my_lock.unlock();
                    return;
                }
            } else {
                up_sub[0] = r1;
                up_sub[1] = g1;
                up_sub[2] = b1;
            }
        }
    }
}


void similar_arithmetic_1(Mat mGr, uchar *d, int row, int col, int *str_start_rgb,
                          vector<vector<int> > str_sub_rgb, vector<vector<int> > str_sub_xy,
                          int rect_w, int rect_y, int *range_, double sim, int *return_value) {
    double target = sim;
    int c1, c2, c3, c4, c12;
    int index;
    int threshold = (int) (765 * (1 - target));
    int up_sub[3] = {-1, -1, -1};
    int up_sub1[3] = {-1, -1, -1};
    vector<vector<int> > str_sub_xy_copy(str_sub_xy);
    vector<vector<int> > str_sub_rgb_copy(str_sub_rgb);

    for (int i = range_[1]; i < range_[3]; ++i) {
        d = mGr.ptr<uchar>(i);
        for (int j = range_[0]; j < range_[2]; ++j) {
            if (return_value[0] > -1) {
                return;
            }
            index = j * 3;
            int r1, g1, b1;
            r1 = d[index];
            g1 = d[index + 1];
            b1 = d[index + 2];
            c1 = r1 - str_start_rgb[0];
            c1 = (c1 ^ (c1 >> 31)) - (c1 >> 31);
            c2 = g1 - str_start_rgb[1];
            c2 = (c2 ^ (c2 >> 31)) - (c2 >> 31);

            c12 = c1 + c2;
            c3 = b1 - str_start_rgb[2];
            c3 = (c3 ^ (c3 >> 31)) - (c3 >> 31);//绝对值
            c4 = c12 + c3;
            if (c4 < threshold) {
                int x = j;
                int y = i;
                bool check = true;
                for (size_t k = 0; k < str_sub_xy.size(); ++k) {
                    int row = i + str_sub_xy[k][1];
                    int col = j + str_sub_xy[k][0];
                    if (row < 0 || row >= rect_y || col < 0 || col >= rect_w) {
                        check = false;
                        break;
                    }
                    index = col * 3;
                    r1 = mGr.ptr<uchar>(row)[index];
                    g1 = mGr.ptr<uchar>(row)[index + 1];
                    b1 = mGr.ptr<uchar>(row)[index + 2];


                    c1 = r1 - str_sub_rgb[k][0];
                    c1 = (c1 ^ (c1 >> 31)) - (c1 >> 31);

                    c2 = g1 - str_sub_rgb[k][1];
                    c2 = (c2 ^ (c2 >> 31)) - (c2 >> 31);
                    c12 = c1 + c2;

                    c3 = b1 - str_sub_rgb[k][2];
                    c3 = (c3 ^ (c3 >> 31)) - (c3 >> 31);
                    c4 = c12 + c3;
                    if (c4 > threshold) {
                        if (k > 0) {
                            str_sub_xy[0][0] = str_sub_xy_copy[k][0];
                            str_sub_xy[0][1] = str_sub_xy_copy[k][1];
                            str_sub_xy[k][0] = str_sub_xy_copy[0][0];
                            str_sub_xy[k][1] = str_sub_xy_copy[0][1];
                            str_sub_xy_copy.assign(str_sub_xy.begin(), str_sub_xy.end());
                            str_sub_rgb[0][0] = str_sub_rgb_copy[k][0];
                            str_sub_rgb[0][1] = str_sub_rgb_copy[k][1];
                            str_sub_rgb[0][2] = str_sub_rgb_copy[k][2];
                            str_sub_rgb[k][0] = str_sub_rgb_copy[0][0];
                            str_sub_rgb[k][1] = str_sub_rgb_copy[0][1];
                            str_sub_rgb[k][2] = str_sub_rgb_copy[0][2];
                            str_sub_rgb_copy.assign(str_sub_rgb.begin(), str_sub_rgb.end());
                        }
                        check = false;
                        break;
                    }
                }
                /*
                thread threads[str_sub_xy.size()];
                for (size_t k = 0; k < str_sub_xy.size(); k++) {
                    threads[k] = thread(sub_similar_arithmetic, i, j, str_sub_xy, str_sub_rgb,
                                        rect_y, rect_w, mGr, threshold, &check, k);
                }
                for (size_t k = 0; k < str_sub_xy.size(); k++) {
                    threads[k].join();
                }
                */

                if (check) {
//                    LOGI("======multipointFindColor==========================lock==============================");
                    my_lock.lock();
                    if (return_value[0] > -1) {
//                        LOGI("======multipointFindColor==========================return==============================");
                        my_lock.unlock();
                        return;
                    }
//                    LOGI("======multipointFindColor==========================return==============================%lf ;%d ;%d ",sim, x, y);
                    return_value[0] = x;
                    return_value[1] = y;
//                    LOGI("======multipointFindColor==========================unlock==============================");
                    my_lock.unlock();
                    return;
                }
            }
        }
    }
}


void sub_similar_arithmetic(int i, int j, vector<vector<int> > str_sub_xy,
                            vector<vector<int> > str_sub_rgb, int rect_y, int rect_w, Mat mGr,
                            int threshold, bool *check, int k) {
    int c1, c2, c3, c4, index;
    int row = i + str_sub_xy[k][1];
    int col = j + str_sub_xy[k][0];
//LOGI("======multipointFindColor==========================1==============================%d %d ",row,col);
// LOGI("======multipointFindColor==========================1==============================%d %d ",row,col);
    if (row < 0 || row > rect_y || col < 0 || col > rect_w) {
        my_lock.lock();
        *check = false;
        my_lock.unlock();
        return;
    }
    index = col * 3;
    int r1 = mGr.ptr<uchar>(row)[index];
    int g1 = mGr.ptr<uchar>(row)[index + 1];
    int b1 = mGr.ptr<uchar>(row)[index + 2];
    c1 = r1 - str_sub_rgb[k][0];
    c1 = (c1 ^ (c1 >> 31)) - (c1 >> 31);
//                    if (c1>threshold){
//                        check = false;
//                        break;
//                    }
    c2 = g1 - str_sub_rgb[k][1];
    c2 = (c2 ^ (c2 >> 31)) - (c2 >> 31);
    c3 = b1 - str_sub_rgb[k][2];
    c3 = (c3 ^ (c3 >> 31)) - (c3 >> 31);
    c4 = c1 + c2 + c3;
//                    sim = c4 / 765.0;
    if (c4 > threshold) {
//        LOGI("======multipointFindColor==========================1==============================check  false ");
//                   if(sim < target){
        my_lock.lock();
        *check = false;
        my_lock.unlock();
//        LOGI("======multipointFindColor==========================unlock==============================check_sub ");
        return;
    }


}




//#endif


