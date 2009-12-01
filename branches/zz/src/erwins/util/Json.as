package erwins.util{
	import com.adobe.serialization.json.*;
	
	import mx.collections.ArrayCollection;
	import mx.controls.*;
	import mx.controls.listClasses.ListBase;
	import mx.core.UIComponent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.*;
	
	/** 
	 * json에는 기본적으로 오류정보(isSuccess)와 실제 메시지(message) 2가지가 담겨있다. 
	 * */
	public class Json{
		
		private static var CHILDREN:String = "children";
		
		private var json:Object;
		
		public static function decode(str:String):Object{
			var json:Object;
			try{
				json = JSON.decode(str);
			}catch(e:Error){
				var message:String  = "오류메세지 : " + e.message + "\n";
				message += "원문 : " + str;
				Alert.show(message," == JSON 파싱에 실패했습니다. ==.");
			}
			return json;
		}
		
		/**
		 * 항상 결과값을 json으로 파싱해준다. 
		 **/
		public function Json(e:ResultEvent){
			var str:String = e.result.toString();
			json = decode(str);
		}
		
		/**
		 * 알려진 정규 예외를 던지면 이곳에서만 받는다.
		 * ex) 암호가 틀렸습니다 등.
		 * 100% 성공해야하는 단순 검색이 아니라면 모두 이것을 사용해야 한다.
		 **/ 
		public function result(fun:Function,fail:Function=null):void{
			if(json.isSuccess){
				fun(json.message);
			} else{
				if(fail!=null) fail(json.message);
				else Alert.show(json.message);
			}
		}
		
		public function getMessage():String{			
			return json.message as String;
		}
		public function getObject(key:String=null):Object{
			if(key==null) return json.message;
			else return json.message[key];
		}
		
		/** option은 동일 멤버필드로 있는 method이다. */
		public function getArray(key:String=null,option:Function=null):ArrayCollection{
			
			var result:ArrayCollection;
			if(key==null) result = new ArrayCollection(json.message);
			else result = new ArrayCollection(json.message[key]);
			
			if(option!=null) option(result);
			return result;
		}
		
		/* ==================================== oprion ===========================================   */		
		/** json에 checked속성을 추가해준다. */
		public static const CHECKED:Function = function checked(json:ArrayCollection):void{
			for each(var obj:Object in json){
				obj.type = 'check';
			}
		}
		
		/** json에 checked속성을 추가해준다. */
		/*
		public static function checked(json:ArrayCollection):void{
			for each(var obj:Object in json){
				obj.type = 'check';
			}
		}*/
		
		/* ==================================== static ===========================================   */
		
		/** json형태인지? 즉 단순 dynamic Object인지? */
		public static function isReflexive(obj:Object):Boolean{
			if(obj is Array || obj is ArrayCollection || obj.toString() == '[object Object]') return true;
			return false;
		}
		
		/**
		 * 간단하게 내용물을 보여준다.
		 **/ 
		public static function show(obj:Object,alert:Boolean=false):String{
			var str:String = "";
			for(var key:String in obj){
				str += key + " : " + obj[key].toString() + "\n";
			}
			if(alert) Alert.show(str,"테스트 내용물입니다.");
			return str;
		}		
		
		/**
		 * 자식(children)이 있다면 true, 아라면 false를 리턴한다.  
		 **/
		public static function isBranch(json:Object):Boolean{
			return json[CHILDREN] == null ? false: true; 
		}
		
		/**
		 * 알려진 컴포넌트에 value를 세팅한다.
		 * Object형태의 json date의 경우 값은 "value"를 사용한다.
		 **/  
		public static function setValue(component:UIComponent,value:String):void{
			if(component==null) return;
			if(component is Label){
				var label:Label = component as Label;						
				label.htmlText = value;
			}else if(component is TextInput){
				var textInput:TextInput = component as TextInput;
				textInput.text = value;
			}else if(component is TextArea){
				var textArea:TextArea = component as TextArea;
				textArea.text = value;
			}else if(component is ComboBox){
				var comboBox:ComboBox = component as ComboBox;
				var oo:ArrayCollection = comboBox.dataProvider as ArrayCollection;
				for(var i:int=0;i<oo.length;i++){
					if(oo[i].id == value || oo[i].value == value){
						comboBox.selectedIndex = i; break;
					}
				}
			}
		}
		
		/**
		 * 알려진 컴포넌트의 value를 가져온다.
		 * Object형태의 json date의 경우 값은 id로 먼저 찾고 없으면 value를 찾는다.
		 **/  
		public static function getValue(component:UIComponent):String{
			if(component==null) return null;
			if(component is Label){
				var label:Label = component as Label;						
				return label.text;
			}else if(component is TextInput){
				var textInput:TextInput = component as TextInput;
				return textInput.text;
			}else if(component is TextArea){
				var textArea:TextArea = component as TextArea;
				return textArea.text;
			}else if(component is ComboBox){
				var comboBox:ComboBox = component as ComboBox;
				var result:String = comboBox.selectedItem.id;
				if(result==null) result = comboBox.selectedItem.value;
				return result;
			}else if(component is ListBase){
				var listBase:ListBase = component as ListBase;
				var listResult:String = listBase.selectedItem.id;
				if(listResult==null) listResult = listBase.selectedItem.value;
				return listResult;
			}
			return null;
		}
		
		/**
		 * BaseUIComponent에 json을 update한다.
		 * 입력될 컴포넌트는 base에 직접 접근 가능한 유일 id를 가지고 있어야 한다. 
		 **/
		public static function update(base:UIComponent,json:Object):void{
			
			if(json==null) return;
			
			if(json["id"]!=null) json["entityId"] =  json["id"] ; //키워드인 id를 변경해주자.
			
			for(var key:String in json){
				var temp:UIComponent = null;
				try{
					temp = base[key];
				} catch(e:Error){
					//non
				}
				setValue(temp,json[key].toString());
				
			}
		}
		
		/** 
		 * 1단계만을 고려한다.  최초의 ?는 직접 입력해야 한다.
		 * 파일업로드나 get방식에 사용된다. (post의 경우 Http내장함수가 알아서 해준다.)
		 * 가변인자 또는 Object형을  지원한다.
		 **/
		public static function serialize(json:Object):String{
			var buff:String = '';
			for(var key:String in json){
				if(buff!='') buff+= '&';
				var obj:Object = json[key];
				if(obj is Array){
					for each(var part:Object in obj){
						buff += key + '='+obj.toString();
					}
				}else buff += key + '='+obj.toString();
			}
			return buff;
		}
		
		/** B의 내용을 A로 복사한다. 오버라이딩 된다. */
		public static function merge(a:Object,b:Object):void{
			for(var key:String in b) a[key] = b[key];
		}
		
	}
}