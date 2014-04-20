package cn.nwpu.museum.db;

import cn.nwpu.museum.bean.Exhibit;
import cn.nwpu.museum.bean.Hall;
import cn.nwpu.museum.service.ExhibitService;
import cn.nwpu.museum.service.HallService;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

	/**
	 * 
	 * @param context
	 * @param name  数据库名称
	 * @param factory 使用系统默认的工厂
	 * @param version 数据库版本号
	 */
	private Context context;
	public DBOpenHelper(Context context) {
		super(context, "museum.db", null, 1);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL( "CREATE TABLE hall (hallid integer primary key autoincrement, name varchar(20), positionx integer, positiony integer,hallnumber integer unique)");
	    db.execSQL("CREATE TABLE  exhibit (exhibitid integer primary key autoincrement, name varchar, description varchar, exhibitnumber integer unique, hallnumber integer," +
	    		" foreign key(hallnumber) references hall(hallnumber) on delete cascade on update cascade); ");
	    //this.initDB(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
	    db.execSQL("ALTER TABLE hall ADD hallnumber integer unique");
	}

	
	private void initDB(SQLiteDatabase db){
		HallService hs = new HallService(context);
		hs.save(new Hall(-1,"铜车马陈列室",50,20,1));
		hs.save(new Hall(-1,"第一号坑 秦兵马佣展览厅",60,50,2));
		hs.save(new Hall(-1,"第二号坑",50,10,3));
		hs.save(new Hall(-1,"第三号坑",20,20,4));
		hs.save(new Hall(-1,"出土兵器陈列室",30,80,5));
		ExhibitService es = new ExhibitService(context);
		es.save(new Exhibit(-1,"一号车（立车）", " 一号车车前驾四匹铜马，车舆平面呈横长方形，前边两角呈弧形，舆宽74厘米，进深48．5厘米。车輢(车箱两旁人可以倚靠的木板)较低，四面敞露，车舆内竖立着一个高杠铜伞，伞下有一立姿御官俑，车上配有铜弩、铜盾、铜箭镞等兵器。《后汉书·舆服志》刘昭注引徐广日：“立乘日高车，坐乘日安车。”可见一号车应该是秦始皇乘舆中的立车，又名高车。一号车虽然有伞但四周敞露，又配有兵器，实质上应该是兵车。蔡邕在《独断》中记述秦始皇法驾卤簿的车马仪仗时曾说：“又有戎立车以征伐”，说明了立车在皇帝车队中用以开道、警卫和征伐的作用。"
		,1, 1));
		es.save(new Exhibit(-1,"二号车（安车）","铜车马是秦始皇的陪葬品之一，象征着秦始皇銮驾的一部分。铜车马的形制是模仿实实在在真车的形状。以前考古发现的车都是木质的，出土时已腐朽，铜车马的出土使我们能够清楚地看到古代御用车驾的真实面貌。铜车马是按秦始皇御用车队中属车二分之一的比例缩小制成的，车盖以及车舆内外彩绘着精美的纹样；两乘铜车马上的金银饰品重达14公斤，显示了铜车马高贵的等级。",2,1));
		es.save(new Exhibit(-1,"铜车马","这两乘车均为单辕、双轮、四马系驾。中间两匹马称为服马，主要用来驾辕；旁边两匹马协助服马拉车，称为骖马。为了使四匹马各处其位，齐力拉车，铜车马上还装有防止骖马内靠及外逸的专用部件——胁驱和缰绳，胁驱的作用就是防止骖马内靠；为了防止骖马外逸，在骖马的颈上还套有一根缰绳，缰绳的另一端系在衡、轭的交接处，防止骖马过分远离。通过这些装置，骖马与服马始终会保持一定的距离，既不会太远，也不会太近，可以始终并驾齐驱。",3,1));
	    es.save(new Exhibit(-1,"立姿御官俑","伞下有一立姿御官俑，车上配有铜弩、铜盾、铜箭镞等兵器。《后汉书·舆服志》刘昭注引徐广日：“立乘日高车，坐乘日安车。”可见一号车应该是秦始皇乘舆中的立车，又名高车。一号车虽然有伞但四周敞露，又配有兵器，实质上应该是兵车。蔡邕在《独断》中记述秦始皇法驾卤簿的车马仪仗时曾说：“又有戎立车以征伐”，说明了立车在皇帝车队中用以开道、警卫和征伐的作用。",4,1));
	}
}
