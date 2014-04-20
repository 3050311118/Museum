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
		hs.save(new Hall(-1,"ͭ���������",50,20,1));
		hs.save(new Hall(-1,"��һ�ſ� �ر���Ӷչ����",60,50,2));
		hs.save(new Hall(-1,"�ڶ��ſ�",50,10,3));
		hs.save(new Hall(-1,"�����ſ�",20,20,4));
		hs.save(new Hall(-1,"��������������",30,80,5));
		ExhibitService es = new ExhibitService(getContext());
		es.save(new Exhibit(-1,"һ�ų���������", " һ�ų���ǰ����ƥͭ������ƽ��ʺ᳤���Σ�ǰ�����ǳʻ��Σ��߿�74���ף�����48��5���ס����}(���������˿����п���ľ��)�ϵͣ����注¶��������������һ���߸�ͭɡ��ɡ����һ��������ٸ����������ͭ��ͭ�ܡ�ͭ���ߵȱ����������顤�߷�־������ע������գ��������ո߳��������հ��������ɼ�һ�ų�Ӧ������ʼ�ʳ����е������������߳���һ�ų���Ȼ��ɡ�����ܳ�¶�������б�����ʵ����Ӧ���Ǳ����������ڡ����ϡ��м�����ʼ�ʷ���±���ĳ�������ʱ��˵������������������������˵���������ڻʵ۳��������Կ��������������������á�"
		,1, 1));
		es.save(new Exhibit(-1,"���ų���������","ͭ��������ʼ�ʵ�����Ʒ֮һ����������ʼ���Ǽݵ�һ���֡�ͭ�����������ģ��ʵʵ�����泵����״����ǰ���ŷ��ֵĳ�����ľ�ʵģ�����ʱ�Ѹ��࣬ͭ����ĳ���ʹ�����ܹ�����ؿ����Ŵ����ó��ݵ���ʵ��ò��ͭ�����ǰ���ʼ�����ó�������������֮һ�ı�����С�Ƴɵģ������Լ���������ʻ��ž���������������ͭ�����ϵĽ�����Ʒ�ش�14�����ʾ��ͭ����߹�ĵȼ���",2,1));
		es.save(new Exhibit(-1,"ͭ����","�����˳���Ϊ��ԯ��˫�֡�����ϵ�ݡ��м���ƥ���Ϊ������Ҫ������ԯ���Ա���ƥ��Э��������������Ϊ����Ϊ��ʹ��ƥ�������λ������������ͭ�����ϻ�װ�з�ֹ�����ڿ������ݵ�ר�ò�������в����������в�������þ��Ƿ�ֹ�����ڿ���Ϊ�˷�ֹ�������ݣ�������ľ��ϻ�����һ����������������һ��ϵ�ں⡢��Ľ��Ӵ�����ֹ�������Զ�롣ͨ����Щװ�ã����������ʼ�ջᱣ��һ���ľ��룬�Ȳ���̫Զ��Ҳ����̫��������ʼ�ղ���������",3,1));
	    es.save(new Exhibit(-1,"��������ٸ","ɡ����һ��������ٸ����������ͭ��ͭ�ܡ�ͭ���ߵȱ����������顤�߷�־������ע������գ��������ո߳��������հ��������ɼ�һ�ų�Ӧ������ʼ�ʳ����е������������߳���һ�ų���Ȼ��ɡ�����ܳ�¶�������б�����ʵ����Ӧ���Ǳ����������ڡ����ϡ��м�����ʼ�ʷ���±���ĳ�������ʱ��˵������������������������˵���������ڻʵ۳��������Կ��������������������á�",4,1));
	
	    es.save(new Exhibit(-1,"����ٸ","�й��Ŵ��Ĳ�����������ڹ�Ԫǰ21���͵��Ĵ���������ʷ�ķ�չ�����ı�Ǩ������Ҳ������һ���൱���ķ�չʱ�ڡ����ض��ԣ��Խ�����ͳһ���������Ĳ����ɳ���������죬�Ⱥ������ɼ򵥵Ķ���������������ս����ͽ�����ٵ��������������׶Σ����ճ�Ϊ�ھ����ж������ƣ���ս��������������ֽ�����ս��һ��ǿ��ı��֡�",5,2));
	    es.save(new Exhibit(-1,"���ٸ","����ٸ���ڳ�����һ�����ٸ�Ͱ��������ѳ�����������㣬����ٸ����Ӧ������ʰ���116ƥ��ÿƥ��ǰ������ʿٸһ������ٸ�����������׼ȷ��������棬�ų�һ��������ĳ������Ρ���Щ���ٸ���ع�����������¼��Ҳ���й�����ʷ�Ϸ��ֵ������϶ࡢʱ����������ٸȺ�������о��й��������չʷ���ش��ľ���ʷ������Ҫ��ֵ��",6,2));
	    es.save(new Exhibit(-1,"����ٸ","����ս��ʱ�ڣ����������ľ���ǿ������ս��ǧ�ˡ�����ƥ��������������ʼ������ƾ������֧ǿ�����װ�������沢�������������ͳһ�����Ĵ�ҵ��һ���������ű���ٸ������صĴ���ս���ͳ�ʿٸ������������֤����",7,2));
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
		hs.update(new Hall(1,"ͭ���������",50,29,1 ));
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
