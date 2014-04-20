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
	 * @param name  ���ݿ�����
	 * @param factory ʹ��ϵͳĬ�ϵĹ���
	 * @param version ���ݿ�汾��
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
		hs.save(new Hall(-1,"ͭ���������",50,20,1));
		hs.save(new Hall(-1,"��һ�ſ� �ر���Ӷչ����",60,50,2));
		hs.save(new Hall(-1,"�ڶ��ſ�",50,10,3));
		hs.save(new Hall(-1,"�����ſ�",20,20,4));
		hs.save(new Hall(-1,"��������������",30,80,5));
		ExhibitService es = new ExhibitService(context);
		es.save(new Exhibit(-1,"һ�ų���������", " һ�ų���ǰ����ƥͭ������ƽ��ʺ᳤���Σ�ǰ�����ǳʻ��Σ��߿�74���ף�����48��5���ס����}(���������˿����п���ľ��)�ϵͣ����注¶��������������һ���߸�ͭɡ��ɡ����һ��������ٸ����������ͭ��ͭ�ܡ�ͭ���ߵȱ����������顤�߷�־������ע������գ��������ո߳��������հ��������ɼ�һ�ų�Ӧ������ʼ�ʳ����е������������߳���һ�ų���Ȼ��ɡ�����ܳ�¶�������б�����ʵ����Ӧ���Ǳ����������ڡ����ϡ��м�����ʼ�ʷ���±���ĳ�������ʱ��˵������������������������˵���������ڻʵ۳��������Կ��������������������á�"
		,1, 1));
		es.save(new Exhibit(-1,"���ų���������","ͭ��������ʼ�ʵ�����Ʒ֮һ����������ʼ���Ǽݵ�һ���֡�ͭ�����������ģ��ʵʵ�����泵����״����ǰ���ŷ��ֵĳ�����ľ�ʵģ�����ʱ�Ѹ��࣬ͭ����ĳ���ʹ�����ܹ�����ؿ����Ŵ����ó��ݵ���ʵ��ò��ͭ�����ǰ���ʼ�����ó�������������֮һ�ı�����С�Ƴɵģ������Լ���������ʻ��ž���������������ͭ�����ϵĽ�����Ʒ�ش�14�����ʾ��ͭ����߹�ĵȼ���",2,1));
		es.save(new Exhibit(-1,"ͭ����","�����˳���Ϊ��ԯ��˫�֡�����ϵ�ݡ��м���ƥ���Ϊ������Ҫ������ԯ���Ա���ƥ��Э��������������Ϊ����Ϊ��ʹ��ƥ�������λ������������ͭ�����ϻ�װ�з�ֹ�����ڿ������ݵ�ר�ò�������в����������в�������þ��Ƿ�ֹ�����ڿ���Ϊ�˷�ֹ�������ݣ�������ľ��ϻ�����һ����������������һ��ϵ�ں⡢��Ľ��Ӵ�����ֹ�������Զ�롣ͨ����Щװ�ã����������ʼ�ջᱣ��һ���ľ��룬�Ȳ���̫Զ��Ҳ����̫��������ʼ�ղ���������",3,1));
	    es.save(new Exhibit(-1,"��������ٸ","ɡ����һ��������ٸ����������ͭ��ͭ�ܡ�ͭ���ߵȱ����������顤�߷�־������ע������գ��������ո߳��������հ��������ɼ�һ�ų�Ӧ������ʼ�ʳ����е������������߳���һ�ų���Ȼ��ɡ�����ܳ�¶�������б�����ʵ����Ӧ���Ǳ����������ڡ����ϡ��м�����ʼ�ʷ���±���ĳ�������ʱ��˵������������������������˵���������ڻʵ۳��������Կ��������������������á�",4,1));
	}
}
