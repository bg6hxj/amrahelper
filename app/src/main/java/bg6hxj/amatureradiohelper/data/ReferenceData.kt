package bg6hxj.amatureradiohelper.data

/**
 * Q简语数据类
 */
data class QCode(
    val code: String,      // Q简语代码
    val question: String,  // 问句意思
    val answer: String     // 答句意思
)

/**
 * 频率对照表数据类
 */
data class FrequencyBand(
    val frequency: String, // 频率范围
    val category: String   // 类别（A/B/C级别）
)

/**
 * 常用缩略语数据类
 */
data class Abbreviation(
    val index: Int,         // 序号
    val code: String,       // 缩略语
    val english: String,    // 英文原意
    val chinese: String     // 中文含义
)

/**
 * 全球呼号分区数据类
 */
data class CallsignPrefix(
    val prefix: String,        // 呼号系列
    val allocatedTo: String,   // 划分给
    val allocatedToEn: String  // Allocated to
)

/**
 * 字母解释法数据类
 */
data class PhoneticAlphabet(
    val letter: String,    // 字母
    val standard: String,  // 标准读法
    val others: String     // 其他读法
)

/**
 * 参考资料数据源
 */
object ReferenceData {
    
    /**
     * 全球呼号分区数据
     */
    val callsignPrefixes: List<CallsignPrefix> = listOf(
        CallsignPrefix("AAA-ALZ", "美利坚合众国", "United States of America"),
        CallsignPrefix("AMA-AOZ", "西班牙", "Spain"),
        CallsignPrefix("APA-ASZ", "巴基斯坦伊斯兰共和国", "Pakistan (Islamic Republic of)"),
        CallsignPrefix("ATA-AWZ", "印度共和国", "India (Republic of)"),
        CallsignPrefix("AXA-AXZ", "澳大利亚", "Australia"),
        CallsignPrefix("AYA-AZZ", "阿根廷共和国", "Argentine Republic"),
        CallsignPrefix("A2A-A2Z", "博茨瓦纳共和国", "Botswana (Republic of)"),
        CallsignPrefix("A3A-A3Z", "汤加王国", "Tonga (Kingdom of)"),
        CallsignPrefix("A4A-A4Z", "阿曼苏丹国", "Oman (Sultanate of)"),
        CallsignPrefix("A5A-A5Z", "不丹王国", "Bhutan (Kingdom of)"),
        CallsignPrefix("A6A-A6Z", "阿拉伯联合酋长国", "United Arab Emirates"),
        CallsignPrefix("A7A-A7Z", "卡塔尔国", "Qatar (State of)"),
        CallsignPrefix("A8A-A8Z", "利比里亚共和国", "Liberia (Republic of)"),
        CallsignPrefix("A9A-A9Z", "巴林国", "Bahrain (Kingdom of)"),
        CallsignPrefix("BAA-BZZ", "中华人民共和国", "China (People's Republic of)"),
        CallsignPrefix("CAA-CEZ", "智利", "Chile"),
        CallsignPrefix("CFA-CKZ", "加拿大", "Canada"),
        CallsignPrefix("CLA-CMZ", "古巴", "Cuba"),
        CallsignPrefix("CNA-CNZ", "摩洛哥王国", "Morocco (Kingdom of)"),
        CallsignPrefix("COA-COZ", "古巴", "Cuba"),
        CallsignPrefix("CPA-CPZ", "玻利维亚共和国", "Bolivia (Republic of)"),
        CallsignPrefix("CQA-CUZ", "葡萄牙", "Portugal"),
        CallsignPrefix("CVA-CXZ", "乌拉圭东岸共和国", "Uruguay (Eastern Republic of)"),
        CallsignPrefix("CYA-CZZ", "加拿大", "Canada"),
        CallsignPrefix("C2A-C2Z", "瑙鲁共和国", "Nauru (Republic of)"),
        CallsignPrefix("C3A-C3Z", "安道尔公国", "Andorra (Principality of)"),
        CallsignPrefix("C4A-C4Z", "塞浦路斯共和国", "Cyprus (Republic of)"),
        CallsignPrefix("C5A-C5Z", "冈比亚共和国", "Gambia (Republic of the)"),
        CallsignPrefix("C6A-C6Z", "巴哈马国", "Bahamas (Commonwealth of the)"),
        CallsignPrefix("C7A-C7Z", "世界气象组织", "World Meteorological Organization"),
        CallsignPrefix("C8A-C9Z", "莫桑比克共和国", "Mozambique (Republic of)"),
        CallsignPrefix("DAA-DRZ", "德意志联邦共和国", "Germany (Federal Republic of)"),
        CallsignPrefix("DSA-DTZ", "大韩民国", "Korea (Republic of)"),
        CallsignPrefix("DUA-DZZ", "菲律宾共和国", "Philippines (Republic of the)"),
        CallsignPrefix("D2A-D3Z", "安哥拉共和国", "Angola (Republic of)"),
        CallsignPrefix("D4A-D4Z", "佛得角共和国", "Cape Verde (Republic of)"),
        CallsignPrefix("D5A-D5Z", "利比里亚共和国", "Liberia (Republic of)"),
        CallsignPrefix("D6A-D6Z", "科摩罗伊斯兰联邦共和国", "Comoros (Union of)"),
        CallsignPrefix("D7A-D9Z", "大韩民国", "Korea (Republic of)"),
        CallsignPrefix("EAA-EHZ", "西班牙", "Spain"),
        CallsignPrefix("EIA-EJZ", "爱尔兰", "Ireland"),
        CallsignPrefix("EKA-EKZ", "亚美尼亚共和国", "Armenia (Republic of)"),
        CallsignPrefix("ELA-ELZ", "利比里亚共和国", "Liberia (Republic of)"),
        CallsignPrefix("EMA-EOZ", "乌克兰", "Ukraine"),
        CallsignPrefix("EPA-EQZ", "伊朗伊斯兰共和国", "Iran (Islamic Republic of)"),
        CallsignPrefix("ERA-ERZ", "摩尔多瓦共和国", "Moldova (Republic of)"),
        CallsignPrefix("ESA-ESZ", "爱沙尼亚共和国", "Estonia (Republic of)"),
        CallsignPrefix("ETA-ETZ", "埃塞俄比亚联邦民主共和国", "Ethiopia (Federal Democratic Republic of)"),
        CallsignPrefix("EUA-EWZ", "白俄罗斯共和国", "Belarus (Republic of)"),
        CallsignPrefix("EXA-EXZ", "吉尔吉斯共和国", "Kyrgyz Republic"),
        CallsignPrefix("EYA-EYZ", "塔吉克斯坦共和国", "Tajikistan (Republic of)"),
        CallsignPrefix("EZA-EZZ", "土库曼斯坦共和国", "Turkmenistan"),
        CallsignPrefix("E2A-E2Z", "泰国", "Thailand"),
        CallsignPrefix("E3A-E3Z", "厄立特里亚", "Eritrea"),
        CallsignPrefix("E4A-E4Z", "巴勒斯坦当局¹", "Palestinian Authority¹"),
        CallsignPrefix("E5A-E5Z", "新西兰 -- 库克群岛", "New Zealand -- Cook Islands"),
        CallsignPrefix("E7A-E7Z", "波斯尼亚和黑塞哥维那", "Bosnia and Herzegovina"),
        CallsignPrefix("FAA-FZZ", "法国", "France"),
        CallsignPrefix("GAA-GZZ", "大不列颠及北爱尔兰联合王国", "United Kingdom of Great Britain and Northern Ireland"),
        CallsignPrefix("HAA-HAZ", "匈牙利共和国", "Hungary (Republic of)"),
        CallsignPrefix("HBA-HBZ", "瑞士联邦", "Switzerland (Confederation of)"),
        CallsignPrefix("HCA-HDZ", "厄瓜多尔", "Ecuador"),
        CallsignPrefix("HEA-HEZ", "瑞士联邦", "Switzerland (Confederation of)"),
        CallsignPrefix("HFA-HFZ", "波兰共和国", "Poland (Republic of)"),
        CallsignPrefix("HGA-HGZ", "匈牙利共和国", "Hungary (Republic of)"),
        CallsignPrefix("HHA-HHZ", "海地共和国", "Haiti (Republic of)"),
        CallsignPrefix("HIA-HIZ", "多米尼加共和国", "Dominican Republic"),
        CallsignPrefix("HJA-HKZ", "哥伦比亚共和国", "Colombia (Republic of)"),
        CallsignPrefix("HLA-HLZ", "大韩民国", "Korea (Republic of)"),
        CallsignPrefix("HMA-HMZ", "朝鲜民主主义人民共和国", "Democratic People's Republic of Korea"),
        CallsignPrefix("HNA-HNZ", "伊拉克共和国", "Iraq (Republic of)"),
        CallsignPrefix("HOA-HPZ", "巴拿马共和国", "Panama (Republic of)"),
        CallsignPrefix("HQA-HRZ", "洪都拉斯共和国", "Honduras (Republic of)"),
        CallsignPrefix("HAS-HSZ", "泰国", "Thailand"),
        CallsignPrefix("HTA-HTZ", "尼加拉瓜", "Nicaragua"),
        CallsignPrefix("HUA-HUZ", "萨尔瓦多共和国", "El Salvador (Republic of)"),
        CallsignPrefix("HVA-HVZ", "梵蒂冈", "Vatican City State"),
        CallsignPrefix("HWA-HYZ", "法国", "France"),
        CallsignPrefix("HZA-HZZ", "沙特阿拉伯王国", "Saudi Arabia (Kingdom of)"),
        CallsignPrefix("H2A-H2Z", "塞浦路斯共和国", "Cyprus (Republic of)"),
        CallsignPrefix("H3A-H3Z", "巴拿马共和国", "Panama (Republic of)"),
        CallsignPrefix("H4A-H4Z", "所罗门群岛", "Solomon Islands"),
        CallsignPrefix("H6A-H7Z", "尼加拉瓜", "Nicaragua"),
        CallsignPrefix("H8A-H9Z", "巴拿马共和国", "Panama (Republic of)"),
        CallsignPrefix("IAA-IZZ", "意大利", "Italy"),
        CallsignPrefix("JAA-JSZ", "日本", "Japan"),
        CallsignPrefix("JTA-JVZ", "蒙古", "Mongolia"),
        CallsignPrefix("JWA-JXZ", "挪威", "Norway"),
        CallsignPrefix("JYA-JYZ", "约旦哈希姆王国", "Jordan (Hashemite Kingdom of)"),
        CallsignPrefix("JZA-JZZ", "印度尼西亚共和国", "Indonesia (Republic of)"),
        CallsignPrefix("J2A-J2Z", "吉布提共和国", "Djibouti (Republic of)"),
        CallsignPrefix("J3A-J3Z", "格林纳达", "Grenada"),
        CallsignPrefix("J4A-J4Z", "希腊", "Greece"),
        CallsignPrefix("J5A-J5Z", "几内亚比绍共和国", "Guinea-Bissau (Republic of)"),
        CallsignPrefix("J6A-J6Z", "圣卢西亚岛", "Saint Lucia"),
        CallsignPrefix("J7A-J7Z", "多米尼克国", "Dominica (Commonwealth of)"),
        CallsignPrefix("J8A-J8Z", "圣文森特和格林纳丁斯", "Saint Vincent and the Grenadines"),
        CallsignPrefix("KAA-KZZ", "美利坚合众国", "United States of America"),
        CallsignPrefix("LAA-LNZ", "挪威", "Norway"),
        CallsignPrefix("LOA-LWZ", "阿根廷共和国", "Argentine Republic"),
        CallsignPrefix("LXA-LXZ", "卢森堡", "Luxembourg"),
        CallsignPrefix("LYA-LYZ", "立陶宛共和国", "Lithuania (Republic of)"),
        CallsignPrefix("LZA-LZZ", "保加利亚共和国", "Bulgaria (Republic of)"),
        CallsignPrefix("L2A-L9Z", "阿根廷共和国", "Argentine Republic"),
        CallsignPrefix("MAA-MZZ", "大不列颠及北爱尔兰联合王国", "United Kingdom of Great Britain and Northern Ireland"),
        CallsignPrefix("NAA-NZZ", "美利坚合众国", "United States of America"),
        CallsignPrefix("OAA-OCZ", "秘鲁", "Peru"),
        CallsignPrefix("ODA-ODZ", "黎巴嫩", "Lebanon"),
        CallsignPrefix("OEA-OEZ", "奥地利", "Austria"),
        CallsignPrefix("OFA-OJZ", "芬兰", "Finland"),
        CallsignPrefix("OKA-OLZ", "捷克共和国", "Czech Republic"),
        CallsignPrefix("OMA-OMZ", "斯洛伐克共和国", "Slovak Republic"),
        CallsignPrefix("ONA-OTZ", "比利时", "Belgium"),
        CallsignPrefix("OUA-OZZ", "丹麦", "Denmark"),
        CallsignPrefix("PAA-PIZ", "荷兰王国", "Netherlands (Kingdom of the)"),
        CallsignPrefix("PJA-PJZ", "荷属安的列斯群岛", "Netherlands (Kingdom of the) -- Netherlands Antilles"),
        CallsignPrefix("PKA-POZ", "印度尼西亚共和国", "Indonesia (Republic of)"),
        CallsignPrefix("PPA-PYZ", "巴西联邦共和国", "Brazil (Federative Republic of)"),
        CallsignPrefix("PZA-PZZ", "苏里南共和国", "Suriname (Republic of)"),
        CallsignPrefix("P2A-P2Z", "巴布亚新几内亚", "Papua New Guinea"),
        CallsignPrefix("P3A-P3Z", "塞浦路斯共和国", "Cyprus (Republic of)"),
        CallsignPrefix("P4A-P4Z", "荷属阿鲁巴", "Netherlands (Kingdom of the) -- Aruba"),
        CallsignPrefix("P5A-P9Z", "朝鲜民主主义人民共和国", "Democratic People's Republic of Korea"),
        CallsignPrefix("RAA-RZZ", "俄罗斯联邦", "Russian Federation"),
        CallsignPrefix("SAA-SMZ", "瑞典", "Sweden"),
        CallsignPrefix("SNA-SRZ", "波兰共和国", "Poland (Republic of)"),
        CallsignPrefix("SSA-SSM", "阿拉伯埃及共和国", "Egypt (Arab Republic of)"),
        CallsignPrefix("SSN-STZ", "苏丹共和国", "Sudan (Republic of the)"),
        CallsignPrefix("SUA-SUZ", "阿拉伯埃及共和国", "Egypt (Arab Republic of)"),
        CallsignPrefix("SVA-SZZ", "希腊", "Greece"),
        CallsignPrefix("S2A-S3Z", "孟加拉人民共和国", "Bangladesh (People's Republic of)"),
        CallsignPrefix("S5A-S5Z", "斯洛文尼亚共和国", "Slovenia (Republic of)"),
        CallsignPrefix("S6A-S6Z", "新加坡共和国", "Singapore (Republic of)"),
        CallsignPrefix("S7A-S7Z", "塞舌尔共和国", "Seychelles (Republic of)"),
        CallsignPrefix("S8A-S8Z", "南非共和国", "South Africa (Republic of)"),
        CallsignPrefix("S9A-S9Z", "圣多美和普林西比民主共和国", "Sao Tome and Principe (Democratic Republic of)"),
        CallsignPrefix("TAA-TCZ", "土耳其", "Turkey"),
        CallsignPrefix("TDA-TDZ", "危地马拉共和国", "Guatemala (Republic of)"),
        CallsignPrefix("TEA-TEZ", "哥斯达黎加", "Costa Rica"),
        CallsignPrefix("TFA-TFZ", "冰岛", "Iceland"),
        CallsignPrefix("TGA-TGZ", "危地马拉共和国", "Guatemala (Republic of)"),
        CallsignPrefix("THA-THZ", "法国", "France"),
        CallsignPrefix("TIA-TIZ", "哥斯达黎加", "Costa Rica"),
        CallsignPrefix("TJA-TJZ", "喀麦隆共和国", "Cameroon (Republic of)"),
        CallsignPrefix("TKA-TKZ", "法国", "France"),
        CallsignPrefix("TLA-TLZ", "中非共和国", "Central African Republic"),
        CallsignPrefix("TMA-TMZ", "法国", "France"),
        CallsignPrefix("TNA-TNZ", "刚果共和国", "Congo (Republic of the)"),
        CallsignPrefix("TOA-TQZ", "法国", "France"),
        CallsignPrefix("TRA-TRZ", "加蓬共和国", "Gabonese Republic"),
        CallsignPrefix("TSA-TSZ", "突尼斯", "Tunisia"),
        CallsignPrefix("TTA-TTZ", "乍得共和国", "Chad (Republic of)"),
        CallsignPrefix("TUA-TUZ", "科特迪瓦共和国", "Côte d'Ivoire (Republic of)"),
        CallsignPrefix("TVA-TXZ", "法国", "France"),
        CallsignPrefix("TYA-TYZ", "贝宁共和国", "Benin (Republic of)"),
        CallsignPrefix("TZA-TZZ", "马里共和国", "Mali (Republic of)"),
        CallsignPrefix("T2A-T2Z", "图瓦卢", "Tuvalu"),
        CallsignPrefix("T3A-T3Z", "基里巴斯共和国", "Kiribati (Republic of)"),
        CallsignPrefix("T4A-T4Z", "古巴", "Cuba"),
        CallsignPrefix("T5A-T5Z", "索马里民主共和国", "Somali Democratic Republic"),
        CallsignPrefix("T6A-T6Z", "阿富汗伊斯兰国", "Afghanistan"),
        CallsignPrefix("T7A-T7Z", "圣马力诺共和国", "San Marino (Republic of)"),
        CallsignPrefix("T8A-T8Z", "帕劳共和国", "Palau (Republic of)"),
        CallsignPrefix("UAA-UIZ", "俄罗斯联邦", "Russian Federation"),
        CallsignPrefix("UJA-UMZ", "乌兹别克斯坦共和国", "Uzbekistan (Republic of)"),
        CallsignPrefix("UNA-UQZ", "哈萨克斯坦共和国", "Kazakhstan (Republic of)"),
        CallsignPrefix("URA-UZZ", "乌克兰", "Ukraine"),
        CallsignPrefix("VAA-VGZ", "加拿大", "Canada"),
        CallsignPrefix("VHZ-VNZ", "澳大利亚", "Australia"),
        CallsignPrefix("VOA-VOZ", "加拿大", "Canada"),
        CallsignPrefix("VPA-VQZ", "大不列颠及北爱尔兰联合王国", "United Kingdom of Great Britain and Northern Ireland"),
        CallsignPrefix("VRA-VRZ", "中华人民共和国 --- 香港", "China (People's Republic of) -- Hong Kong"),
        CallsignPrefix("VSA-VSZ", "大不列颠及北爱尔兰联合王国", "United Kingdom of Great Britain and Northern Ireland"),
        CallsignPrefix("VTA-VWZ", "印度共和国", "India (Republic of)"),
        CallsignPrefix("VXA-VYZ", "加拿大", "Canada"),
        CallsignPrefix("VZA-VZZ", "澳大利亚", "Australia"),
        CallsignPrefix("V2A-V2Z", "安提瓜和巴布达", "Antigua and Barbuda"),
        CallsignPrefix("V3A-V3Z", "伯利兹", "Belize"),
        CallsignPrefix("V4A-V4Z", "圣基茨和尼维斯", "Saint Kitts and Nevis"),
        CallsignPrefix("V5A-V5Z", "纳米比亚共和国", "Namibia (Republic of)"),
        CallsignPrefix("V6A-V6Z", "密克罗尼西亚联邦", "Micronesia (Federated States of)"),
        CallsignPrefix("V7A-V7Z", "马绍尔群岛共和国", "Marshall Islands (Republic of the)"),
        CallsignPrefix("V8A-V8Z", "文莱达鲁萨兰国", "Brunei Darussalam"),
        CallsignPrefix("WAA-WZZ", "美利坚合众国", "United States of America"),
        CallsignPrefix("XAA-XIZ", "墨西哥", "Mexico"),
        CallsignPrefix("XJA-XOZ", "加拿大", "Canada"),
        CallsignPrefix("XPA-XPZ", "丹麦", "Denmark"),
        CallsignPrefix("XQA-XRZ", "智利", "Chile"),
        CallsignPrefix("XSA-XSZ", "中华人民共和国", "China (People's Republic of)"),
        CallsignPrefix("XTA-XTZ", "布基纳法索", "Burkina Faso"),
        CallsignPrefix("XUA-XUZ", "柬埔寨王国", "Cambodia (Kingdom of)"),
        CallsignPrefix("XVA-XVZ", "越南社会主义共和国", "Viet Nam (Socialist Republic of)"),
        CallsignPrefix("XXA-XXZ", "中华人民共和国 -- 澳门", "China (People's Republic of) -- Macao"),
        CallsignPrefix("XWA-XWZ", "老挝人民民主共和国", "Lao People's Democratic Republic"),
        CallsignPrefix("XYA-XZZ", "缅甸联邦", "Myanmar (Union of)"),
        CallsignPrefix("YAA-YAZ", "阿富汗伊斯兰国", "Afghanistan"),
        CallsignPrefix("YBA-YHZ", "印度尼西亚共和国", "Indonesia (Republic of)"),
        CallsignPrefix("YIA-YIZ", "伊拉克共和国", "Iraq (Republic of)"),
        CallsignPrefix("YJA-YJZ", "瓦努阿图共和国", "Vanuatu (Republic of)"),
        CallsignPrefix("YKA-YKZ", "阿拉伯叙利亚共和国", "Syrian Arab Republic"),
        CallsignPrefix("YLA-YLZ", "拉脱维亚共和国", "Latvia (Republic of)"),
        CallsignPrefix("YMA-YMZ", "土耳其", "Turkey"),
        CallsignPrefix("YNA-YNZ", "尼加拉瓜", "Nicaragua"),
        CallsignPrefix("YOA-YRZ", "罗马尼亚", "Romania"),
        CallsignPrefix("YSA-YSZ", "萨尔瓦多共和国", "El Salvador (Republic of)"),
        CallsignPrefix("YTA-YUZ", "塞尔维亚（共和国）", "Serbia (Republic of)"),
        CallsignPrefix("YVA-YYZ", "委内瑞拉共和国", "Venezuela (Bolivarian Republic of)"),
        CallsignPrefix("Y2A-Y9Z", "德意志联邦共和国", "Germany (Federal Republic of)"),
        CallsignPrefix("ZAA-ZAZ", "阿尔巴尼亚共和国", "Albania (Republic of)"),
        CallsignPrefix("ZBA-ZJZ", "大不列颠及北爱尔兰联合王国", "United Kingdom of Great Britain and Northern Ireland"),
        CallsignPrefix("ZKA-ZMZ", "新西兰", "New Zealand"),
        CallsignPrefix("ZNA-ZOZ", "大不列颠及北爱尔兰联合王国", "United Kingdom of Great Britain and Northern Ireland"),
        CallsignPrefix("ZPA-ZPZ", "巴拉圭共和国", "Paraguay (Republic of)"),
        CallsignPrefix("ZQA-ZQZ", "大不列颠及北爱尔兰联合王国", "United Kingdom of Great Britain and Northern Ireland"),
        CallsignPrefix("ZRA-ZUZ", "南非共和国", "South Africa (Republic of)"),
        CallsignPrefix("ZVA-ZZZ", "巴西联邦共和国", "Brazil (Federative Republic of)"),
        CallsignPrefix("Z2A-Z2Z", "津巴布韦共和国", "Zimbabwe (Republic of)"),
        CallsignPrefix("Z3A-Z3Z", "前南斯拉夫的马其顿共和国", "The Former Yugoslav Republic of Macedonia"),
        CallsignPrefix("2AA-2ZZ", "大不列颠及北爱尔兰联合王国", "United Kingdom of Great Britain and Northern Ireland"),
        CallsignPrefix("3AA-3AZ", "摩纳哥公国", "Monaco (Principality of)"),
        CallsignPrefix("3BA-3BZ", "毛里求斯共和国", "Mauritius (Republic of)"),
        CallsignPrefix("3CA-3CZ", "赤道几内亚共和国", "Equatorial Guinea (Republic of)"),
        CallsignPrefix("3DA-3DM", "斯威士兰王国", "Swaziland (Kingdom of)"),
        CallsignPrefix("3DN-3DZ", "斐济共和国", "Fiji (Republic of)"),
        CallsignPrefix("3EA-3FZ", "巴拿马共和国", "Panama (Republic of)"),
        CallsignPrefix("3GA-3GZ", "智利", "Chile"),
        CallsignPrefix("3HA-3UZ", "中华人民共和国", "China (People's Republic of)"),
        CallsignPrefix("3VA-3VZ", "突尼斯", "Tunisia"),
        CallsignPrefix("3WA-3WZ", "越南社会主义共和国", "Viet Nam (Socialist Republic of)"),
        CallsignPrefix("3XA-3XZ", "几内亚共和国", "Guinea (Republic of)"),
        CallsignPrefix("3YA-3YZ", "挪威", "Norway"),
        CallsignPrefix("3ZA-3ZZ", "波兰共和国", "Poland (Republic of)"),
        CallsignPrefix("4AA-4CZ", "墨西哥", "Mexico"),
        CallsignPrefix("4DA-4IZ", "菲律宾共和国", "Philippines (Republic of the)"),
        CallsignPrefix("4JA-4KZ", "阿塞拜疆共和国", "Azerbaijani Republic"),
        CallsignPrefix("4LA-4LZ", "格鲁吉亚", "Georgia"),
        CallsignPrefix("4MA-4MZ", "委内瑞拉共和国", "Venezuela (Bolivarian Republic of)"),
        CallsignPrefix("4OA-4OZ", "黑山（共和国）", "Montenegro (Republic of)"),
        CallsignPrefix("4PA-4SZ", "斯里兰卡民主社会主义共和国", "Sri Lanka (Democratic Socialist Republic of)"),
        CallsignPrefix("4TA-4TZ", "秘鲁", "Peru"),
        CallsignPrefix("4UA-4UZ", "联合国", "United Nations"),
        CallsignPrefix("4VA-4VZ", "海地共和国", "Haiti (Republic of)"),
        CallsignPrefix("4WA-4WZ", "东帝汶民主共和国", "Democratic Republic of Timor-Leste"),
        CallsignPrefix("4XA-4XZ", "以色列国", "Israel (State of)"),
        CallsignPrefix("4YA-4YZ", "国际民航组织", "International Civil Aviation Organization"),
        CallsignPrefix("4ZA-4ZZ", "以色列国", "Israel (State of)"),
        CallsignPrefix("5AA-5AZ", "阿拉伯利比亚人民社会主义民众国", "Socialist People's Libyan Arab Jamahiriya"),
        CallsignPrefix("5BA-5BZ", "塞浦路斯共和国", "Cyprus (Republic of)"),
        CallsignPrefix("5CA-5GZ", "摩洛哥王国", "Morocco (Kingdom of)"),
        CallsignPrefix("5HA-5IZ", "坦桑尼亚联合共和国", "Tanzania (United Republic of)"),
        CallsignPrefix("5JA-5KZ", "哥伦比亚共和国", "Colombia (Republic of)"),
        CallsignPrefix("5LA-5MZ", "利比里亚共和国", "Liberia (Republic of)"),
        CallsignPrefix("5NA-5OZ", "尼日利亚联邦共和国", "Nigeria (Federal Republic of)"),
        CallsignPrefix("5PA-5QZ", "丹麦", "Denmark"),
        CallsignPrefix("5RA-5SZ", "马达加斯加共和国", "Madagascar (Republic of)"),
        CallsignPrefix("5TA-5TZ", "毛里塔尼亚伊斯兰共和国", "Mauritania (Islamic Republic of)"),
        CallsignPrefix("5UA-5UZ", "尼日尔共和国", "Niger (Republic of the)"),
        CallsignPrefix("5VA-5VZ", "多哥共和国", "Togolese Republic"),
        CallsignPrefix("5WA-5WZ", "西萨摩亚独立国", "Samoa (Independent State of)"),
        CallsignPrefix("5XA-5XZ", "乌干达共和国", "Uganda (Republic of)"),
        CallsignPrefix("5YA-5ZZ", "肯尼亚共和国", "Kenya (Republic of)"),
        CallsignPrefix("6AA-6BZ", "阿拉伯埃及共和国", "Egypt (Arab Republic of)"),
        CallsignPrefix("6CA-6CZ", "阿拉伯叙利亚共和国", "Syrian Arab Republic"),
        CallsignPrefix("6DA-6JZ", "墨西哥", "Mexico"),
        CallsignPrefix("6KA-6NZ", "大韩民国", "Korea (Republic of)"),
        CallsignPrefix("6OA-6OZ", "索马里民主共和国", "Somali Democratic Republic"),
        CallsignPrefix("6PA-6SZ", "巴基斯坦伊斯兰共和国", "Pakistan (Islamic Republic of)"),
        CallsignPrefix("6TA-6UZ", "苏丹共和国", "Sudan (Republic of the)"),
        CallsignPrefix("6VA-6WZ", "塞内加尔共和国", "Senegal (Republic of)"),
        CallsignPrefix("6XA-6XZ", "马达加斯加共和国", "Madagascar (Republic of)"),
        CallsignPrefix("6YA-6YZ", "牙买加", "Jamaica"),
        CallsignPrefix("6ZA-6ZZ", "利比里亚共和国", "Liberia (Republic of)"),
        CallsignPrefix("7AA-7IZ", "印度尼西亚共和国", "Indonesia (Republic of)"),
        CallsignPrefix("7JA-7NZ", "日本", "Japan"),
        CallsignPrefix("7OA-7OZ", "也门共和国", "Yemen (Republic of)"),
        CallsignPrefix("7PA-7PZ", "莱索托王国", "Lesotho (Kingdom of)"),
        CallsignPrefix("7QA-7QZ", "马拉维", "Malawi"),
        CallsignPrefix("7RA-7RZ", "阿尔及利亚民主人民共和国", "Algeria (People's Democratic Republic of)"),
        CallsignPrefix("7SA-7SZ", "瑞典", "Sweden"),
        CallsignPrefix("7TA-7YZ", "阿尔及利亚民主人民共和国", "Algeria (People's Democratic Republic of)"),
        CallsignPrefix("7ZA-7ZZ", "沙特阿拉伯王国", "Saudi Arabia (Kingdom of)"),
        CallsignPrefix("8AA-8IZ", "印度尼西亚共和国", "Indonesia (Republic of)"),
        CallsignPrefix("8JA-8NZ", "日本", "Japan"),
        CallsignPrefix("8OA-8OZ", "博茨瓦纳共和国", "Botswana (Republic of)"),
        CallsignPrefix("8PA-8PZ", "巴巴多斯", "Barbados"),
        CallsignPrefix("8QA-8QZ", "马尔代夫共和国", "Maldives (Republic of)"),
        CallsignPrefix("8RA-8RZ", "圭亚那", "Guyana"),
        CallsignPrefix("8SA-8SZ", "瑞典", "Sweden"),
        CallsignPrefix("8TA-8YZ", "印度共和国", "India (Republic of)"),
        CallsignPrefix("8ZA-8ZZ", "沙特阿拉伯王国", "Saudi Arabia (Kingdom of)"),
        CallsignPrefix("9AA-9AZ", "克罗地亚共和国", "Croatia (Republic of)"),
        CallsignPrefix("9BA-9DZ", "伊朗伊斯兰共和国", "Iran (Islamic Republic of)"),
        CallsignPrefix("9EA-9FZ", "埃塞俄比亚联邦民主共和国", "Ethiopia (Federal Democratic Republic of)"),
        CallsignPrefix("9GA-9GZ", "加纳", "Ghana"),
        CallsignPrefix("9HA-9HZ", "马耳他", "Malta"),
        CallsignPrefix("9IA-9JZ", "赞比亚共和国", "Zambia (Republic of)"),
        CallsignPrefix("9KA-9KZ", "科威特国", "Kuwait (State of)"),
        CallsignPrefix("9LA-9LZ", "塞拉利昂", "Sierra Leone"),
        CallsignPrefix("9MA-9MZ", "马来西亚", "Malaysia"),
        CallsignPrefix("9NA-9NZ", "尼泊尔", "Nepal"),
        CallsignPrefix("9OA-9TZ", "刚果民主共和国", "Democratic Republic of the Congo"),
        CallsignPrefix("9UA-9UZ", "布隆迪共和国", "Burundi (Republic of)"),
        CallsignPrefix("9VA-9VZ", "新加坡共和国", "Singapore (Republic of)"),
        CallsignPrefix("9WA-9WZ", "马来西亚", "Malaysia"),
        CallsignPrefix("9XA-9XZ", "卢旺达共和国", "Rwandese Republic"),
        CallsignPrefix("9YA-9ZZ", "特立尼达和多巴哥", "Trinidad and Tobago")
    )

    /**
     * 常用Q简语数据
     */
    val qCodes: List<QCode> = listOf(
        QCode("QRA", "你的电台名称是？", "我的电台名称是..."),
        QCode("QRB", "你台离我台多远？", "我们相距约为..."),
        QCode("QRG", "我的准确频率是多少？", "你的准确频率是..."),
        QCode("QRI", "我的音调如何？", "你的音调是(T1-T9)"),
        QCode("QRJ", "我的信号小吗？", "你的信号小"),
        QCode("QRK", "我的信号可辨度是多少？", "你的信号可辨度是"),
        QCode("QRL", "你忙吗？", "我正忙"),
        QCode("QRM", "你受到他台干扰吗？", "我正受到他台干扰"),
        QCode("QRN", "你受到天电干扰吗？", "我正受到天电干扰"),
        QCode("QRO", "要我增加发信功率吗？", "请增加发信功率"),
        QCode("QRP", "要我减低发信功率吗？", "请减低发信功率"),
        QCode("QRQ", "要我发得快些吗？", "请发快些"),
        QCode("QRS", "要我发得慢些吗？", "请发慢些"),
        QCode("QRT", "要我停止拍发吗？", "请停止拍发"),
        QCode("QRU", "你有事吗？", "无事"),
        QCode("QRV", "你准备好了吗？", "我已准备好了"),
        QCode("QRW", "需要我转告吗？", "请转告"),
        QCode("QRX", "要我等多长时间？", "请等待... ...分钟"),
        QCode("QRZ", "谁在呼叫我？", "...正在呼叫你"),
        QCode("QSA", "我的信号强度是多少？", "你的信号强度是..."),
        QCode("QSB", "我的信号有衰落吗？", "你的信号强度是"),
        QCode("QSD", "我的信号不完整吗？", "你的信号不完整"),
        QCode("QSL", "你确认收妥/QSL卡片吗？", "我确认收妥/QSL卡片"),
        QCode("QSO", "你能否和...通信？", "你能和...通信"),
        QCode("QSP", "你能中转到...吗？", "我能中转到..."),
        QCode("QSU", "能在这个频率回复吗？", "我将在此频率回复"),
        QCode("QSV", "有天电干扰要我在此频率发一串 V 字吗？", "请在此频率发一串 V 字"),
        QCode("QSW", "你将在此频率发吗？", "我将在此频率发"),
        QCode("QSX", "你将在某频率收听吗？", "我将在某频率收听"),
        QCode("QSY", "要我改用其他频率拍发吗？", "请改用...KHz/MHz拍发"),
        QCode("QSZ", "要我每组发两遍吗？", "请每组发两遍"),
        QCode("QTB", "要我查对组数吗？", "请查对组数"),
        QCode("QTC", "你有几份报要发？", "我有...份报要发"),
        QCode("QTH", "你的地理位置是？", "我的地理位置是...")
    )
    
    /**
     * 频率对照表数据
     */
    val frequencyBands: List<FrequencyBand> = listOf(
        FrequencyBand("1800-2000 KHz", "BC"),
        FrequencyBand("3500-3900 KHz", "BC"),
        FrequencyBand("7000-7200 KHz", "BC"),
        FrequencyBand("10100-10150 KHz", "BC"),
        FrequencyBand("14000-14250 KHz", "BC"),
        FrequencyBand("14250-14350 KHz", "BC"),
        FrequencyBand("18068-18168 KHz", "BC"),
        FrequencyBand("21000-21450 KHz", "BC"),
        FrequencyBand("24890-24990 KHz", "BC"),
        FrequencyBand("28000-29700 KHz", "BC"),
        FrequencyBand("50-54 MHz", "ABC"),
        FrequencyBand("144-148 MHz", "ABC"),
        FrequencyBand("430-440 MHz", "ABC")
    )
    
    /**
     * 常用缩略语数据
     */
    val abbreviations: List<Abbreviation> = listOf(
        Abbreviation(1, "AB", "ALL BEFORE", "在前"),
        Abbreviation(2, "ABT", "ABOUT", "关于、大约"),
        Abbreviation(3, "CFM", "CONFIRM", "确认、认为"),
        Abbreviation(4, "CUAGN", "SEE YOU AGAIN", "再见到你"),
        Abbreviation(5, "CUL", "SEE YOU LATER", "再会"),
        Abbreviation(6, "CW", "CONTINUOUS WAVE", "等幅电报"),
        Abbreviation(7, "DE", "FROM", "从"),
        Abbreviation(8, "DF", "DIRECTION FINDING", "测向"),
        Abbreviation(9, "DR", "DEAR", "亲爱的"),
        Abbreviation(10, "NW", "NOW", "现在"),
        Abbreviation(11, "R", "ROGER, RECEIVED", "收到了"),
        Abbreviation(12, "AA", "ALL AFTER", "在后"),
        Abbreviation(13, "ADD", "ADDITION", "增加"),
        Abbreviation(14, "ADR", "ADDRESS", "地址"),
        Abbreviation(15, "AF", "AFRICA", "非洲"),
        Abbreviation(16, "AGN", "AGAIN", "再、再一次"),
        Abbreviation(17, "AHR", "ANOTHER", "其他、另外"),
        Abbreviation(18, "AM", "AMPLITUDE MODULATION", "调幅"),
        Abbreviation(19, "ANS", "ANSWER", "回答"),
        Abbreviation(20, "ANT", "ANTENNA", "天线"),
        Abbreviation(21, "AS", "ASIA", "亚洲"),
        Abbreviation(22, "BCI", "BROADCAST INTERFERENCE", "广播干扰"),
        Abbreviation(23, "BCNU", "BE SEEING YOU", "见到你"),
        Abbreviation(24, "BGN", "BEGIN", "开始"),
        Abbreviation(25, "BUG", "SEMI-AUTOMATIC KEY", "半自动电键"),
        Abbreviation(26, "BURO", "BUREAU", "管理局"),
        Abbreviation(27, "CD", "CRD", "卡片"),
        Abbreviation(28, "CLBK", "CALL BOOK", "呼号手册"),
        Abbreviation(29, "CQ", "CALL ANY STATION", "普遍呼叫"),
        Abbreviation(30, "CRT", "CORRECTION", "改正"),
        Abbreviation(31, "CY", "COPY", "抄收"),
        Abbreviation(32, "DX", "LONG DISTANCE", "远距离"),
        Abbreviation(33, "EL、ELE", "ANTENNA ELEMENT", "天线单元"),
        Abbreviation(34, "ES", "AND", "和"),
        Abbreviation(35, "EU", "EUROPE", "欧洲"),
        Abbreviation(36, "FB", "FINE BUSINESS", "良好的"),
        Abbreviation(37, "FM", "FREQUENCY MODULATION", "调频"),
        Abbreviation(38, "FREQ", "FREQUENCY", "频率"),
        Abbreviation(39, "GA", "GOOD AFTERNOON、GO AHEAD", "下午好、往下发"),
        Abbreviation(40, "GB", "GOOD BYE", "再见"),
        Abbreviation(41, "GE", "GOOD EVENING", "晚上好"),
        Abbreviation(42, "GL", "GOOD LUCK", "好运"),
        Abbreviation(43, "GLD", "GLAD", "高兴"),
        Abbreviation(44, "GM", "GOOD MORNING", "早上好"),
        Abbreviation(45, "GN", "GOOD NIGHT", "晚安"),
        Abbreviation(46, "HAM", "AMATEUR TRANSMITTER", "业余无线电爱好者"),
        Abbreviation(47, "HPE", "HOPE", "希望"),
        Abbreviation(48, "HR", "HERE", "这里"),
        Abbreviation(49, "HW", "HOW", "如何"),
        Abbreviation(50, "K、KU", "GO AHEAD", "请回答"),
        Abbreviation(51, "KNW", "KNOW", "知道"),
        Abbreviation(52, "LG", "LONG", "长"),
        Abbreviation(53, "LIS", "LICENSE", "执照"),
        Abbreviation(54, "LOG", "LOG BOOK", "电台日记"),
        Abbreviation(55, "LSN", "LISTEN", "收听"),
        Abbreviation(56, "N", "NO", "不"),
        Abbreviation(57, "NA", "NORTH AMERICA", "北美洲"),
        Abbreviation(58, "NCS", "NET CONTROL STATION", "网络控制电台"),
        Abbreviation(59, "ND", "NOTHING DOING", "无事"),
        Abbreviation(60, "NR", "NUMBER", "数目"),
        Abbreviation(61, "OB", "OLD BOY", "老弟"),
        Abbreviation(62, "OC", "OLD CHAP,OCEANIA", "老伙计，大洋洲"),
        Abbreviation(63, "OM", "OLD MAN", "老火腿"),
        Abbreviation(64, "OP", "OPERATOR", "操作员、报务员"),
        Abbreviation(65, "OSCAR", "-", "业余卫星"),
        Abbreviation(66, "OT", "OLD TIME", "老前辈、老资格"),
        Abbreviation(67, "POBOX", "POST OFFICE BOX", "邮政信箱"),
        Abbreviation(68, "PSE", "PLEASE", "请"),
        Abbreviation(69, "PWR", "POWER", "功率"),
        Abbreviation(70, "RFI", "RADIO FREQUENCY INTERFERENCE", "无线电干扰"),
        Abbreviation(71, "RIG", "STATION EQUIPMENT", "电台设备"),
        Abbreviation(72, "RPRT", "REPORT", "报告"),
        Abbreviation(73, "RTTY", "RADIO TELETYP", "无线电传打字"),
        Abbreviation(74, "RX", "RECEIVER", "接收机"),
        Abbreviation(75, "SA", "SOUTH AMERICA", "南美洲"),
        Abbreviation(76, "SK", "END", "结束符号"),
        Abbreviation(77, "SKED", "SCKEDULE", "表格"),
        Abbreviation(78, "SN", "SOON", "立即、不久"),
        Abbreviation(79, "SOS", "SAVE OUR SOULD", "呼救信号"),
        Abbreviation(80, "SRI", "SORRY", "抱歉"),
        Abbreviation(81, "SSB", "SINGLE SIDE BAND", "单边带"),
        Abbreviation(82, "SSTV", "SLOW-SCAN TELEVISION", "慢扫描电视"),
        Abbreviation(83, "STN", "STATION", "电台"),
        Abbreviation(84, "SW", "SHORT WAVE、SWITCH", "短波、开关"),
        Abbreviation(85, "SWL", "SHORT WAVE LISTENER", "短波收听台"),
        Abbreviation(86, "T/R", "TRANSMIT/RECEIVE", "收发信"),
        Abbreviation(87, "TEMP", "TEMPERATRE", "温度"),
        Abbreviation(88, "TEST", "CONTEST", "比赛"),
        Abbreviation(89, "TMW", "TOMORROW", "明天"),
        Abbreviation(90, "TNX、TKS", "THANKS", "谢谢"),
        Abbreviation(91, "TU", "THANKS YOU", "谢谢你"),
        Abbreviation(92, "TX", "TRANSMITTER", "发射机"),
        Abbreviation(93, "U", "YOU", "你"),
        Abbreviation(94, "UR", "YOUR, YOUR ARE", "你的、你是"),
        Abbreviation(95, "UTC", "UNIVERSAL COORDINATED TIME", "世界协调时"),
        Abbreviation(96, "VFO", "VARIABLE FREQUENCY OSEILLATOR", "可变频率振荡器"),
        Abbreviation(97, "VY", "VERY", "很"),
        Abbreviation(98, "WW", "WORLDWIDE", "全世界"),
        Abbreviation(99, "WX", "WEATHER", "天气"),
        Abbreviation(100, "XCVR", "TRANSCEIVER", "收发信机"),
        Abbreviation(101, "XMAS", "CHRISTMAS", "圣诞节"),
        Abbreviation(102, "XTAL", "CRYSTAL", "晶体"),
        Abbreviation(103, "XYL", "WIFE", "妻子"),
        Abbreviation(104, "YL", "YOUNG LADY", "小姐、女报务员"),
        Abbreviation(105, "YM", "YOUNGMAN", "青年人（男）"),
        Abbreviation(106, "73", "BEST REGARDS", "祝愿，致意"),
        Abbreviation(107, "88", "LOVE AND KISSES", "爱与接吻")
    )

    /**
     * 字母解释法数据
     */
    val phoneticAlphabets: List<PhoneticAlphabet> = listOf(
        PhoneticAlphabet("A", "Alfa(Alpha)*", "America"),
        PhoneticAlphabet("B", "Bravo", ""),
        PhoneticAlphabet("C", "Charlie", "China/Canada"),
        PhoneticAlphabet("D", "Delta", "Data/Dixie/David"),
        PhoneticAlphabet("E", "Echo", ""),
        PhoneticAlphabet("F", "Foxtrot", "Florida"),
        PhoneticAlphabet("G", "Golf", ""),
        PhoneticAlphabet("H", "Hotel", ""),
        PhoneticAlphabet("I", "India", "Italy/Indigo"),
        PhoneticAlphabet("J", "Juliett(Juliet)*", "Japan"),
        PhoneticAlphabet("K", "Kilo", ""),
        PhoneticAlphabet("L", "Lima", "London"),
        PhoneticAlphabet("M", "Mike", ""),
        PhoneticAlphabet("N", "November", ""),
        PhoneticAlphabet("O", "Oscar", ""),
        PhoneticAlphabet("P", "Papa", ""),
        PhoneticAlphabet("Q", "Quebec", ""),
        PhoneticAlphabet("R", "Romeo", ""),
        PhoneticAlphabet("S", "Sierra", "Sugar"),
        PhoneticAlphabet("T", "Tango", ""),
        PhoneticAlphabet("U", "Uniform", ""),
        PhoneticAlphabet("V", "Victor", ""),
        PhoneticAlphabet("W", "Whiskey", "White/Washigton"),
        PhoneticAlphabet("X", "Xray(X-Ray)*", ""),
        PhoneticAlphabet("Y", "Yankee", ""),
        PhoneticAlphabet("Z", "Zulu", ""),
        // Numbers
        PhoneticAlphabet("1", "One", ""),
        PhoneticAlphabet("2", "Two", ""),
        PhoneticAlphabet("3", "Tree", ""),
        PhoneticAlphabet("4", "Fower", ""),
        PhoneticAlphabet("5", "Fife", ""),
        PhoneticAlphabet("6", "Six", ""),
        PhoneticAlphabet("7", "Seven", ""),
        PhoneticAlphabet("8", "Eight", ""),
        PhoneticAlphabet("9", "Niner", ""),
        PhoneticAlphabet("0", "Zero", ""),
        PhoneticAlphabet("00", "Hundred", ""),
        PhoneticAlphabet("000", "Thousand", ""),
        PhoneticAlphabet(",", "Decimal", ""),
        // Symbols
        PhoneticAlphabet("/", "Portable", "移动台异地"),
        PhoneticAlphabet("/", "Slash", "固定台异地"),
        PhoneticAlphabet("/", "mobile", "车载台异地")
    )
    /**
     * 图片资源数据类
     */
    data class ImageResource(
        val title: String,
        val url: String
    )

    // Image URLs
    private const val ITU_MAP_URL = "https://www.bd8bzy.net/img/blogs/ITU_CQ_WorldMaps/2013_ITU_CQ_WorldMaps-2.jpg"
    private const val CQ_MAP_URL = "https://www.bd8bzy.net/img/blogs/ITU_CQ_WorldMaps/2013_ITU_CQ_WorldMaps-1.jpg"
    private const val CN_ZONE_MAP_URL = "https://s2.loli.net/2024/07/09/nSigoEeYG5tMCW8.png"
    private const val CW_CODE_CHART_URL = "https://s2.loli.net/2024/07/09/pRyE7YWvwuIlMHA.png"

    /**
     * 获取指定类型的参考图片资源
     */
    fun getReferenceImages(type: String): List<ImageResource> {
        return when (type) {
            "itu_cq_zones" -> listOf(
                ImageResource("ITU分区地图", ITU_MAP_URL),
                ImageResource("CQ分区地图", CQ_MAP_URL)
            )
            "cn_zones" -> listOf(
                ImageResource("国内电台分区地图", CN_ZONE_MAP_URL)
            )
            "cw_codes" -> listOf(
                ImageResource("CW电码表", CW_CODE_CHART_URL)
            )
            else -> emptyList()
        }
    }
    /**
     * 国内电台分区数据类
     */
    data class CnZone(
        val suffix: String,    // 呼号后缀
        val province: String   // 省市
    )

    /**
     * 国内电台分区列表
     */
    val cnZones: List<CnZone> = listOf(
        CnZone("1AA-1XZZ", "北京"),
        CnZone("2AA-2HZZ", "黑龙江"),
        CnZone("2IA-2PZZ", "吉林"),
        CnZone("2QA-2XZZ", "辽宁"),
        CnZone("3AA-3FZZ", "天津"),
        CnZone("3GA-3LZZ", "内蒙古"),
        CnZone("3MA-3RZZ", "河北"),
        CnZone("3SA-3XZZ", "山西"),
        CnZone("4AA-4HZZ", "上海"),
        CnZone("4IA-4PZZ", "山东"),
        CnZone("4QA-4XZZ", "江苏"),
        CnZone("5AA-5XZZ", "浙江"),
        CnZone("5IA-5PZZ", "江西"),
        CnZone("5QA-5XZZ", "福建"),
        CnZone("6AA-6HZZ", "安徽"),
        CnZone("6IA-6PZZ", "河南"),
        CnZone("6QA-6XAA", "河北"),
        CnZone("7AA-7HZZ", "湖南"),
        CnZone("7IA-7PZZ", "广东"),
        CnZone("7QA-7XZZ", "广西"),
        CnZone("7YA-7YZZ", "海南"),
        CnZone("8AA-8FZZ", "四川"),
        CnZone("8GA-8LZZ", "重庆"),
        CnZone("8MA-8RZZ", "贵州"),
        CnZone("8SA-8XZZ", "云南"),
        CnZone("9AA-9FZZ", "陕西"),
        CnZone("9GA-9LZZ", "甘肃"),
        CnZone("9MA-9RZZ", "宁夏"),
        CnZone("9SA-9XZZ", "青海"),
        CnZone("0AA-0FZZ", "新疆"),
        CnZone("0GA-0LZZ", "西藏"),
        CnZone("BV0A-BV9ZZZ", "台湾")
    )
}
