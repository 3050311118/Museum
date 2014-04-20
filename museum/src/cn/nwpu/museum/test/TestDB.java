package cn.nwpu.museum.test;

import cn.nwpu.museum.bean.Exhibit;
import cn.nwpu.museum.bean.Hall;
import cn.nwpu.museum.db.DBOpenHelper;
import cn.nwpu.museum.service.ExhibitService;
import cn.nwpu.museum.service.HallService;
import android.test.AndroidTestCase;
import android.util.Log;

public class TestDB extends AndroidTestCase {
    
	public void testDBcreate() throws Exception{
		DBOpenHelper helper = new DBOpenHelper(getContext());
		helper.getReadableDatabase();
	}
	
	public void testHallSave(){
		HallService hs = new HallService(getContext());
		hs.save(new Hall(-1,"铜车马陈列室",50,20,1));
		hs.save(new Hall(-1,"第一号坑 秦兵马佣展览厅",60,50,2));
		hs.save(new Hall(-1,"第二号坑",50,10,3));
		hs.save(new Hall(-1,"第三号坑",20,20,4));
		hs.save(new Hall(-1,"出土兵器陈列室",30,80,5));
		ExhibitService es = new ExhibitService(getContext());
		es.save(new Exhibit(-1,"一号车（立车）", " 一号车车前驾四匹铜马，车舆平面呈横长方形，前边两角呈弧形，舆宽74厘米，进深48．5厘米。车輢(车箱两旁人可以倚靠的木板)较低，四面敞露，车舆内竖立着一个高杠铜伞，伞下有一立姿御官俑，车上配有铜弩、铜盾、铜箭镞等兵器。《后汉书·舆服志》刘昭注引徐广日：“立乘日高车，坐乘日安车。”可见一号车应该是秦始皇乘舆中的立车，又名高车。一号车虽然有伞但四周敞露，又配有兵器，实质上应该是兵车。蔡邕在《独断》中记述秦始皇法驾卤簿的车马仪仗时曾说：“又有戎立车以征伐”，说明了立车在皇帝车队中用以开道、警卫和征伐的作用。"
		,1, 1));
		es.save(new Exhibit(-1,"二号车（安车）","铜车马是秦始皇的陪葬品之一，象征着秦始皇銮驾的一部分。铜车马的形制是模仿实实在在真车的形状。以前考古发现的车都是木质的，出土时已腐朽，铜车马的出土使我们能够清楚地看到古代御用车驾的真实面貌。铜车马是按秦始皇御用车队中属车二分之一的比例缩小制成的，车盖以及车舆内外彩绘着精美的纹样；两乘铜车马上的金银饰品重达14公斤，显示了铜车马高贵的等级。",2,1));
		es.save(new Exhibit(-1,"铜车马","这两乘车均为单辕、双轮、四马系驾。中间两匹马称为服马，主要用来驾辕；旁边两匹马协助服马拉车，称为骖马。为了使四匹马各处其位，齐力拉车，铜车马上还装有防止骖马内靠及外逸的专用部件——胁驱和缰绳，胁驱的作用就是防止骖马内靠；为了防止骖马外逸，在骖马的颈上还套有一根缰绳，缰绳的另一端系在衡、轭的交接处，防止骖马过分远离。通过这些装置，骖马与服马始终会保持一定的距离，既不会太远，也不会太近，可以始终并驾齐驱。",3,1));
	    es.save(new Exhibit(-1,"立姿御官俑","伞下有一立姿御官俑，车上配有铜弩、铜盾、铜箭镞等兵器。《后汉书·舆服志》刘昭注引徐广日：“立乘日高车，坐乘日安车。”可见一号车应该是秦始皇乘舆中的立车，又名高车。一号车虽然有伞但四周敞露，又配有兵器，实质上应该是兵车。蔡邕在《独断》中记述秦始皇法驾卤簿的车马仪仗时曾说：“又有戎立车以征伐”，说明了立车在皇帝车队中用以开道、警卫和征伐的作用。",4,1));
	
	    es.save(new Exhibit(-1,"步兵俑","中国古代的步兵最早出现在公元前21世纪的夏代。随着历史的发展和社会的变迁，步兵也经历了一个相当长的发展时期。就秦而言，自建国到统一六国，它的步兵由初创走向成熟，先后经历了由简单的独立步兵到隶属于战车的徒兵，再到独立步兵三个阶段，最终成为在军队中独立编制，在战争中配合其他兵种进行作战的一个强大的兵种。",5,2));
	    es.save(new Exhibit(-1,"骑兵俑","二号俑坑内出土了一批骑兵俑和鞍马。根据已出土的情况推算，二号俑坑内应埋藏陶质鞍马116匹，每匹马前立有骑士俑一件。陶俑和陶马的造型准确，形象逼真，排成一列列整齐的长方队形。这些骑兵俑是秦国骑兵的形象记录，也是中国考古史上发现的数量较多、时代较早的骑兵俑群，对于研究中国的骑兵发展史和秦代的军事史具有重要价值。",6,2));
	    es.save(new Exhibit(-1,"车兵俑","春秋战国时期，秦是著名的军事强国，有战车千乘、骑万匹、步兵百余万。秦始皇正是凭借着这支强大的武装力量，兼并了六国，完成了统一六国的大业。一、二、三号兵马俑坑内埋藏的大量战车和车士俑就是最有力的证明。",7,2));
	}
	
	public void testHallFind(){
		HallService hs = new HallService(getContext());
		Hall hall = hs.findByNumber(1);
		if(hall != null){
			Log.i("TestDB", hall.getName() + hall.getPositionx() + ":" + hall.getPositiony());
		}else{
			Log.i("TestDB", "null");
		}
	}
	
	public void testUpdate(){
		HallService hs = new HallService(getContext());
		hs.update(new Hall(1,"铜车马陈列室",50,29,1 ));
		testHallFind();
	}
	
	public void testDelete(){
		HallService hs = new HallService(getContext());
		hs.delete(2);
		hs.delete(3);
		Log.i("TestDB", "count:"+hs.getCount());
	}
	public void testFindAll(){
		HallService hs = new HallService(getContext());
		Log.i("TestDB", "count:"+hs.getCount());
		for(Hall hall : hs.getAll()){
			Log.i("TestDB", hall.getId() + hall.getName() + hall.getPositionx() + ":" + hall.getPositiony());
		}
	}
	
	public void testfindByHall(){
		ExhibitService es = new ExhibitService(getContext());
		for(Exhibit ex : es.findByHall("1")){
			Log.i("TestDB", ex.getName() + " "+ex.getHallnumber() + " " + ex.getDescription() );
		}
		
	}
	
	public void testfindByNumber(){
		
		ExhibitService es = new ExhibitService(getContext());
		Exhibit ex = es.findByNumber(2);
	    Log.i("TestDB", ex.getName() + " "+ex.getHallnumber() + " " + ex.getDescription() );
		
	}
}
