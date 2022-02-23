package com.script.utils;

/**
 * Created by     on 2017/2/9.
 */
public class H264HeaderParser {

    public static int findPPSOffsit(byte[] sps_pps,int offsit, int len){
        for(int i = offsit+4 ;i < sps_pps.length-3; ++i){
            if(Utils.bytesToInt(sps_pps, i) == 1){
                return  i - offsit;
            }
        }
        return -1 ;
    }
    public static class Set{
        int value ;
        int offsit ;
    }
    public static class Resulotion{
        public int width ;
        public int height ;
    }
    public static Set ue(byte[] buffer, int len, int offsit){
        int zeroNum = 0 ;
        while(offsit < len * 8){
            if((buffer[offsit/8] & (0x80 >> (offsit%8))) != 0){
                break ;
            }
            zeroNum++ ;
            offsit++ ;
        }
        offsit++ ;

        int dwRet = 0 ;
        for(int i = 0 ;i < zeroNum ; i++){
            dwRet <<= 1 ;
            if( (buffer[offsit/8] & (0x80 >>(offsit%8))) != 0  ){
                dwRet += 1 ;
            }
            offsit++ ;
        }
        Set set = new Set() ;
        set.value = (1 << zeroNum) -1 + dwRet ;
        set.offsit = offsit ;
        return set ;
    }

    public static Set se(byte[] buffer, int len, int offsit){
        Set ueSet = ue(buffer, len, offsit) ;
        double k = ueSet.value ;
        ueSet.value = (int) Math.ceil(k / 2);
        if(ueSet.value % 2 == 0){
            ueSet.value = - ueSet.value ;
        }
        return ueSet ;
    }

    public static Set u(int bitCount, byte[] buffer, int offsit){
        int dwRet = 0 ;
        for(int i = 0 ; i < bitCount ; ++i){
            dwRet <<= 1 ;
            if( (buffer[offsit/8] & (0x80 >> (offsit %8))) != 0  ){
                dwRet += 1 ;
            }
            offsit++ ;
        }
        Set set = new Set() ;
        set.value = dwRet ;
        set.offsit = offsit ;
        return set ;

    }

    public static Resulotion getResulotion(byte[] data, int len){

        Resulotion resulotion = null ;
        boolean     bSpsComplete = false;
        int offsit = 0 ;
        int dataLeft = len - 4 ;
        byte pdata[] = new byte[dataLeft] ;
        for(int i = 0 ;i < dataLeft ; ++i){
            pdata[i] = data[i+ 4] ;
        }
        Set set = new Set() ;
        set.offsit = offsit ;
        set = u(1, pdata, set.offsit) ;int forbidden_zero_bit = set.value ;
        set = u(2, pdata, set.offsit) ;int nal_ref_idc = set.value ;
        set = u(5, pdata, set.offsit) ;int nal_unit_type = set.value ;
        if(nal_unit_type == 7){
            set = u(8, pdata, set.offsit) ;int profile_idc = set.value ;
            set = u(1, pdata, set.offsit) ;int constraint_set0_flag = set.value ;
            set = u(1, pdata, set.offsit) ;int constraint_set1_flag = set.value ;
            set = u(1, pdata, set.offsit) ;int constraint_set2_flag = set.value ;
            set = u(1, pdata, set.offsit) ;int constraint_set3_flag = set.value ;
            set = u(4, pdata, set.offsit) ;int reserved_zero_4bits  = set.value ;
            set = u(8, pdata, set.offsit) ;int level_idc  = set.value ;
            set = ue(pdata, dataLeft, set.offsit) ; int seq_parameter_set_id = set.value ;
            if (profile_idc == 100 ||  // High profile
                    profile_idc == 110 ||  // High10 profile
                    profile_idc == 122 ||  // High422 profile
                    profile_idc == 244 ||  // High444 Predictive profile
                    profile_idc ==  44 ||  // Cavlc444 profile
                    profile_idc ==  83 ||  // Scalable Constrained High profile (SVC)
                    profile_idc ==  86 ||  // Scalable High Intra profile (SVC)
                    profile_idc == 118 ||  // Stereo High profile (MVC)
                    profile_idc == 128 ||  // Multiview High profile (MVC)
                    profile_idc == 138 ||  // Multiview Depth High profile (MVCD)
                    profile_idc == 144)    // old High444 profile
            {
                set = ue(pdata, dataLeft, set.offsit) ; int chroma_format_idc = set.value ;
                if(chroma_format_idc == 3){
                    set = u(1, pdata, set.offsit) ;int residual_colour_transform_flag  = set.value ;
                }
                set = ue(pdata, dataLeft, set.offsit) ; int bit_depth_luma_minus8 = set.value ;
                set = ue(pdata, dataLeft, set.offsit) ; int bit_depth_chroma_minus8 = set.value ;
                set = u(1, pdata, set.offsit)  ; int qpprime_y_zero_transform_bypass_flag = set.value ;
                set = u(1, pdata, set.offsit)  ; int seq_scaling_matrix_present_flag = set.value ;
                int seq_scaling_list_present_flag[] = new int[8];
                if(seq_scaling_matrix_present_flag != 0){
                    for (int i = 0; i < 8; i++)
                    {
                        set = u(1, pdata, set.offsit)  ; seq_scaling_list_present_flag[i] = set.value ;
                    }

                }
            }

            set = ue(pdata, dataLeft, set.offsit) ; int log2_max_frame_num_minus4 = set.value ;
            set = ue(pdata, dataLeft, set.offsit) ; int pic_order_cnt_type = set.value ;
            if (pic_order_cnt_type == 0)
            {
                set = ue(pdata, dataLeft, set.offsit) ; int log2_max_pic_order_cnt_lsb_minus4 = set.value ;
            }else if (pic_order_cnt_type == 1)
            {
                set = u(1, pdata, set.offsit) ;int delta_pic_order_always_zero_flag  = set.value ;
                set = se(pdata, dataLeft, set.offsit) ; int offset_for_non_ref_pic = set.value ;
                set = se(pdata, dataLeft, set.offsit) ; int offset_for_top_to_bottom_field = set.value ;
                set = ue(pdata, dataLeft, set.offsit) ; int num_ref_frames_in_pic_order_cnt_cycle = set.value ;
                int offset_for_ref_frame[] = new int[num_ref_frames_in_pic_order_cnt_cycle];

                for( int i = 0; i < num_ref_frames_in_pic_order_cnt_cycle; i++ ) {
                    set = se(pdata, dataLeft, set.offsit);offset_for_ref_frame[i] = set.value;
                }
            }
            set = ue(pdata, dataLeft, set.offsit) ; int num_ref_frames = set.value ;
            set = u(1, pdata, set.offsit) ;int gaps_in_frame_num_value_allowed_flag  = set.value ;
            set = ue(pdata, dataLeft, set.offsit) ; int pic_width_in_mbs_minus1 = set.value ;
            set = ue(pdata, dataLeft, set.offsit) ; int pic_height_in_map_units_minus1 = set.value ;
            set = u(1, pdata, set.offsit) ;int frame_mbs_only_flag  = set.value ;
            int width = (pic_width_in_mbs_minus1 + 1) * 16;
            int height = (pic_height_in_map_units_minus1 + 1) * 16;
            int deinterlace = 0;

            if (0 == frame_mbs_only_flag)
            {
                height *= 2;
                deinterlace = 1;
            }
            resulotion = new Resulotion() ;
            resulotion.width = width ;
            resulotion.height = height ;
        }
        return resulotion ;

    }

}
