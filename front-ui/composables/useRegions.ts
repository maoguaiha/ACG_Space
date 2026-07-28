// 中国省市县三级联动数据 (核心省份 + 主要城市)
// 数据来源：国家行政区划 (2019) 标准数据
// 完整版可在 https://github.com/modood/Administrative-divisions-of-China 获取
export interface Region {
  name: string
  code?: string
  cities?: Region[]
}

export const regionsData: Region[] = [
  { name: '北京市', code: '110000', cities: [
    { name: '北京市', code: '110100', cities: [
      { name: '东城区', code: '110101' }, { name: '西城区', code: '110102' },
      { name: '朝阳区', code: '110105' }, { name: '海淀区', code: '110108' },
      { name: '丰台区', code: '110106' }, { name: '石景山区', code: '110107' }
    ]}
  ]},
  { name: '上海市', code: '310000', cities: [
    { name: '上海市', code: '310100', cities: [
      { name: '黄浦区', code: '310101' }, { name: '徐汇区', code: '310104' },
      { name: '长宁区', code: '310105' }, { name: '静安区', code: '310106' },
      { name: '普陀区', code: '310107' }, { name: '虹口区', code: '310109' },
      { name: '浦东新区', code: '310115' }, { name: '闵行区', code: '310112' }
    ]}
  ]},
  { name: '广东省', code: '440000', cities: [
    { name: '广州市', code: '440100', cities: [
      { name: '天河区', code: '440106' }, { name: '越秀区', code: '440104' },
      { name: '海珠区', code: '440105' }, { name: '白云区', code: '440111' },
      { name: '番禺区', code: '440113' }, { name: '南沙区', code: '440115' }
    ]},
    { name: '深圳市', code: '440300', cities: [
      { name: '福田区', code: '440304' }, { name: '罗湖区', code: '440303' },
      { name: '南山区', code: '440305' }, { name: '宝安区', code: '440306' },
      { name: '龙岗区', code: '440307' }, { name: '龙华区', code: '440309' }
    ]},
    { name: '东莞市', code: '441900', cities: [
      { name: '东莞市', code: '441900', cities: [
        { name: '莞城区', code: '441901' }, { name: '东城区', code: '441902' },
        { name: '南城区', code: '441903' }
      ]}
    ]}
  ]},
  { name: '浙江省', code: '330000', cities: [
    { name: '杭州市', code: '330100', cities: [
      { name: '西湖区', code: '330106' }, { name: '上城区', code: '330102' },
      { name: '下城区', code: '330103' }, { name: '拱墅区', code: '330105' },
      { name: '滨江区', code: '330108' }, { name: '余杭区', code: '330110' }
    ]},
    { name: '宁波市', code: '330200', cities: [
      { name: '海曙区', code: '330203' }, { name: '江北区', code: '330205' },
      { name: '鄞州区', code: '330212' }, { name: '镇海区', code: '330211' }
    ]}
  ]},
  { name: '江苏省', code: '320000', cities: [
    { name: '南京市', code: '320100', cities: [
      { name: '玄武区', code: '320102' }, { name: '秦淮区', code: '320104' },
      { name: '建邺区', code: '320105' }, { name: '鼓楼区', code: '320106' },
      { name: '栖霞区', code: '320113' }
    ]},
    { name: '苏州市', code: '320500', cities: [
      { name: '姑苏区', code: '320508' }, { name: '工业园区', code: '320571' },
      { name: '高新区', code: '320572' }, { name: '吴中区', code: '320506' }
    ]}
  ]},
  { name: '四川省', code: '510000', cities: [
    { name: '成都市', code: '510100', cities: [
      { name: '锦江区', code: '510104' }, { name: '青羊区', code: '510105' },
      { name: '金牛区', code: '510106' }, { name: '武侯区', code: '510107' },
      { name: '成华区', code: '510108' }, { name: '高新区', code: '510109' }
    ]}
  ]},
  { name: '湖北省', code: '420000', cities: [
    { name: '武汉市', code: '420100', cities: [
      { name: '江岸区', code: '420102' }, { name: '江汉区', code: '420103' },
      { name: '硚口区', code: '420104' }, { name: '汉阳区', code: '420105' },
      { name: '武昌区', code: '420106' }, { name: '洪山区', code: '420111' }
    ]}
  ]},
  { name: '陕西省', code: '610000', cities: [
    { name: '西安市', code: '610100', cities: [
      { name: '新城区', code: '610102' }, { name: '碑林区', code: '610103' },
      { name: '莲湖区', code: '610104' }, { name: '雁塔区', code: '610113' },
      { name: '未央区', code: '610112' }
    ]}
  ]},
  { name: '天津市', code: '120000', cities: [
    { name: '天津市', code: '120100', cities: [
      { name: '和平区', code: '120101' }, { name: '河东区', code: '120102' },
      { name: '河西区', code: '120103' }, { name: '南开区', code: '120104' },
      { name: '河北区', code: '120105' }, { name: '滨海新区', code: '120116' }
    ]}
  ]},
  { name: '重庆市', code: '500000', cities: [
    { name: '重庆市', code: '500100', cities: [
      { name: '渝中区', code: '500103' }, { name: '江北区', code: '500105' },
      { name: '南岸区', code: '500108' }, { name: '九龙坡区', code: '500107' },
      { name: '渝北区', code: '500112' }, { name: '沙坪坝区', code: '500106' }
    ]}
  ]},
  { name: '福建省', code: '350000', cities: [
    { name: '厦门市', code: '350200', cities: [
      { name: '思明区', code: '350203' }, { name: '湖里区', code: '350206' },
      { name: '海沧区', code: '350205' }, { name: '集美区', code: '350211' }
    ]},
    { name: '福州市', code: '350100', cities: [
      { name: '鼓楼区', code: '350102' }, { name: '台江区', code: '350103' },
      { name: '仓山区', code: '350104' }, { name: '晋安区', code: '350111' }
    ]}
  ]},
  { name: '山东省', code: '370000', cities: [
    { name: '济南市', code: '370100', cities: [
      { name: '历下区', code: '370102' }, { name: '市中区', code: '370103' },
      { name: '槐荫区', code: '370104' }, { name: '天桥区', code: '370105' }
    ]},
    { name: '青岛市', code: '370200', cities: [
      { name: '市南区', code: '370202' }, { name: '市北区', code: '370203' },
      { name: '崂山区', code: '370212' }, { name: '李沧区', code: '370213' }
    ]}
  ]},
  { name: '河南省', code: '410000', cities: [
    { name: '郑州市', code: '410100', cities: [
      { name: '中原区', code: '410102' }, { name: '二七区', code: '410103' },
      { name: '管城区', code: '410104' }, { name: '金水区', code: '410105' }
    ]}
  ]},
  { name: '湖南省', code: '430000', cities: [
    { name: '长沙市', code: '430100', cities: [
      { name: '芙蓉区', code: '430102' }, { name: '天心区', code: '430103' },
      { name: '岳麓区', code: '430104' }, { name: '开福区', code: '430105' },
      { name: '雨花区', code: '430111' }
    ]}
  ]},
  { name: '安徽省', code: '340000', cities: [
    { name: '合肥市', code: '340100', cities: [
      { name: '瑶海区', code: '340102' }, { name: '庐阳区', code: '340103' },
      { name: '蜀山区', code: '340104' }, { name: '包河区', code: '340111' }
    ]}
  ]},
  { name: '江西省', code: '360000', cities: [
    { name: '南昌市', code: '360100', cities: [
      { name: '东湖区', code: '360102' }, { name: '西湖区', code: '360103' },
      { name: '青云谱区', code: '360104' }, { name: '青山湖区', code: '360111' }
    ]}
  ]},
  { name: '云南省', code: '530000', cities: [
    { name: '昆明市', code: '530100', cities: [
      { name: '五华区', code: '530102' }, { name: '盘龙区', code: '530103' },
      { name: '官渡区', code: '530111' }, { name: '西山区', code: '530112' }
    ]}
  ]},
  { name: '广西壮族自治区', code: '450000', cities: [
    { name: '南宁市', code: '450100', cities: [
      { name: '青秀区', code: '450103' }, { name: '兴宁区', code: '450102' },
      { name: '江南区', code: '450105' }, { name: '西乡塘区', code: '450107' }
    ]}
  ]},
  { name: '贵州省', code: '520000', cities: [
    { name: '贵阳市', code: '520100', cities: [
      { name: '南明区', code: '520102' }, { name: '云岩区', code: '520103' },
      { name: '花溪区', code: '520111' }, { name: '乌当区', code: '520112' }
    ]}
  ]},
  { name: '山西省', code: '140000', cities: [
    { name: '太原市', code: '140100', cities: [
      { name: '小店区', code: '140105' }, { name: '迎泽区', code: '140106' },
      { name: '杏花岭区', code: '140107' }, { name: '万柏林区', code: '140109' }
    ]}
  ]},
  { name: '内蒙古自治区', code: '150000', cities: [
    { name: '呼和浩特市', code: '150100', cities: [
      { name: '新城区', code: '150102' }, { name: '回民区', code: '150103' },
      { name: '玉泉区', code: '150104' }, { name: '赛罕区', code: '150105' }
    ]}
  ]},
  { name: '辽宁省', code: '210000', cities: [
    { name: '沈阳市', code: '210100', cities: [
      { name: '和平区', code: '210102' }, { name: '沈河区', code: '210103' },
      { name: '皇姑区', code: '210105' }, { name: '铁西区', code: '210106' }
    ]},
    { name: '大连市', code: '210200', cities: [
      { name: '中山区', code: '210202' }, { name: '西岗区', code: '210203' },
      { name: '沙河口区', code: '210204' }, { name: '甘井子区', code: '210211' }
    ]}
  ]},
  { name: '吉林省', code: '220000', cities: [
    { name: '长春市', code: '220100', cities: [
      { name: '南关区', code: '220102' }, { name: '宽城区', code: '220103' },
      { name: '朝阳区', code: '220104' }, { name: '二道区', code: '220105' },
      { name: '绿园区', code: '220106' }
    ]}
  ]},
  { name: '黑龙江省', code: '230000', cities: [
    { name: '哈尔滨市', code: '230100', cities: [
      { name: '道里区', code: '230102' }, { name: '南岗区', code: '230103' },
      { name: '道外区', code: '230104' }, { name: '香坊区', code: '230110' }
    ]}
  ]},
  { name: '海南省', code: '460000', cities: [
    { name: '海口市', code: '460100', cities: [
      { name: '秀英区', code: '460105' }, { name: '龙华区', code: '460106' },
      { name: '琼山区', code: '460107' }, { name: '美兰区', code: '460108' }
    ]}
  ]},
  { name: '宁夏回族自治区', code: '640000', cities: [
    { name: '银川市', code: '640100', cities: [
      { name: '兴庆区', code: '640104' }, { name: '西夏区', code: '640105' },
      { name: '金凤区', code: '640106' }
    ]}
  ]},
  { name: '新疆维吾尔自治区', code: '650000', cities: [
    { name: '乌鲁木齐市', code: '650100', cities: [
      { name: '天山区', code: '650102' }, { name: '沙依巴克区', code: '650103' },
      { name: '新市区', code: '650104' }, { name: '水磨沟区', code: '650105' }
    ]}
  ]}
]

export function getAllProvinces(): Region[] {
  return regionsData
}

export function getCitiesByProvince(provinceName: string): Region[] {
  const province = regionsData.find(p => p.name === provinceName)
  return province?.cities || []
}

export function getDistrictsByCity(provinceName: string, cityName: string): Region[] {
  const province = regionsData.find(p => p.name === provinceName)
  const city = province?.cities?.find(c => c.name === cityName)
  return city?.cities || []
}