from django.db import models


class FacialFeatureBase(models.Model):
    """
    공통 필드(keyword, desc)를 포함하는 추상 기본 모델.
    각 관상 특징 모델이 이를 상속받아 코드 중복을 줄입니다.
    """
    keyword = models.CharField(max_length=15)
    desc = models.CharField(max_length=100)

    class Meta:
        abstract = True
        managed = False

    def __str__(self):
        return self.keyword

class Matching(models.Model):
    matching_id = models.BigAutoField(primary_key=True)
    requester = models.ForeignKey('User', on_delete=models.CASCADE, related_name='requested_matches')
    accepter = models.ForeignKey('User', on_delete=models.CASCADE, related_name='received_matches')
    created_at = models.DateTimeField(auto_now_add=True)
    compatibility = models.DecimalField(max_digits=4, decimal_places=1)

    class Meta:
        db_table = 'matching'  # 실제 DB 테이블 이름과 일치시킴
        unique_together = ('requester', 'accepter')  # 중복 방지 (선택 사항)

    def __str__(self):
        return f"Matching({self.requester_id} ↔ {self.accepter_id}, {self.compatibility})"
    

class Chin(FacialFeatureBase):
    id = models.BigAutoField(primary_key=True, db_column='chin_parts_id')

    class Meta(FacialFeatureBase.Meta):
        db_table = 'chin_parts'


class Faceshape(FacialFeatureBase):
    id = models.BigAutoField(primary_key=True, db_column='faceshape_id')

    class Meta(FacialFeatureBase.Meta):
        db_table = 'faceshape'


class Mouth(FacialFeatureBase):
    id = models.BigAutoField(primary_key=True, db_column='mouth_parts_id')

    class Meta(FacialFeatureBase.Meta):
        db_table = 'mouth_parts'


class Nose(FacialFeatureBase):
    id = models.BigAutoField(primary_key=True, db_column='nose_parts_id')

    class Meta(FacialFeatureBase.Meta):
        db_table = 'nose_parts'


class Eyebrow(FacialFeatureBase):
    id = models.BigAutoField(primary_key=True, db_column='eyebrow_parts_id')

    class Meta(FacialFeatureBase.Meta):
        db_table = 'eyebrow_parts'

class Eye(FacialFeatureBase):
    id = models.BigAutoField(primary_key=True, db_column='eye_parts_id')

    class Meta(FacialFeatureBase.Meta):
        db_table = 'eye_parts'


class FaceAnalysis(models.Model):
    face_id = models.BigAutoField(primary_key=True)  # 자동 증가 기본키
    img = models.CharField(max_length=150)

    # 조합 ID들은 외래키로 연결할 수도 있고, 단순 정수로 남겨도 됨 (선택 사항)
    faceshape_id = models.BigIntegerField()
    eyebrow_comb_id = models.BigIntegerField()
    eye_comb_id = models.BigIntegerField()
    nose_comb_id = models.BigIntegerField()
    mouth_comb_id = models.BigIntegerField()
    chin_comb_id = models.BigIntegerField()

    summary_analysis = models.CharField(max_length=500)
    personality = models.CharField(max_length=500)
    interpersonal_relationships = models.CharField(max_length=500)
    career_traits = models.CharField(max_length=500)
    life_direction = models.CharField(max_length=500)
    title = models.CharField(max_length=500)
    description = models.CharField(max_length=500)

    

    class Meta:
        db_table = 'face'  
        managed = False  

        
class User(models.Model):
    user_id = models.BigAutoField(primary_key=True)
    email = models.CharField(max_length=250)
    name = models.CharField(max_length=50)
    nickname = models.CharField(max_length=45)
    
    GENDER_CHOICES = (
        ('f', 'Female'),
        ('m', 'Male'),
        ('u', 'Unknown'),
    )
    gender = models.CharField(max_length=1, choices=GENDER_CHOICES)
    
    address = models.CharField(max_length=200)
    latitude = models.FloatField()
    longitude = models.FloatField()

    ROLE_CHOICES = (
        ('USER', 'User'),
        ('ADMIN', 'Admin'),
    )
    role = models.CharField(max_length=5, choices=ROLE_CHOICES)

    created_at = models.DateTimeField()
    last_seen = models.DateTimeField()
    is_online = models.BooleanField()
    
    birth = models.DateField()
    prefer_age_upper = models.IntegerField()
    prefer_age_lower = models.IntegerField()
    is_deleted = models.BooleanField()

    provider = models.CharField(max_length=255)
    social_id = models.CharField(max_length=255)

    face_id = models.BigIntegerField(null=True, blank=True)  

    class Meta:
        db_table = 'user'  
        managed = False        

class EyeComb(models.Model):

    eye_comb_id = models.BigAutoField(primary_key=True) 
    
    # 눈 부위의 첫 번째 ID
    eye_parts_id1 = models.BigIntegerField()
    
    # 눈 부위의 두 번째 ID
    eye_parts_id2 = models.BigIntegerField()
    
    # 눈 부위의 세 번째 ID
    eye_parts_id3 = models.BigIntegerField()
    
    # 조합에 대한 설명 (최대 500자)
    desc = models.CharField(max_length=500)

    class Meta:
        
        db_table = 'eye_comb'
        managed = False


class EyebrowComb(models.Model):

    eyebrow_comb_id = models.BigAutoField(primary_key=True) 
    
    # 눈썹 부위의 첫 번째 ID
    eyebrow_parts_id1 = models.BigIntegerField()
    
    # 눈썹 부위의 두 번째 ID
    eyebrow_parts_id2 = models.BigIntegerField()
    
    # 눈썹 부위의 세 번째 ID
    eyebrow_parts_id3 = models.BigIntegerField()

    # 눈썹 부위의 네 번째 ID
    eyebrow_parts_id4 = models.BigIntegerField()
    
    # 조합에 대한 설명 (최대 500자)
    desc = models.CharField(max_length=500)

    class Meta:
     
        db_table = 'eyebrow_comb'

        managed = False



class MouthComb(models.Model):
    
    mouth_comb_id = models.BigAutoField(primary_key=True) 
    
    # 입 부위의 첫 번째 ID
    mouth_parts_id1 = models.BigIntegerField()
    
    # 입 부위의 두 번째 ID
    mouth_parts_id2 = models.BigIntegerField()
    
    # 입 부위의 세 번째 ID
    mouth_parts_id3 = models.BigIntegerField()
    
    # 조합에 대한 설명 (최대 500자)
    desc = models.CharField(max_length=500)

    class Meta:
    
        db_table = 'mouth_comb'
        managed = False



class NoseComb(models.Model):
  
    nose_comb_id = models.BigAutoField(primary_key=True) 
    
    # 코 부위의 첫 번째 ID
    nose_parts_id1 = models.BigIntegerField()
    
    # 코 부위의 두 번째 ID
    nose_parts_id2 = models.BigIntegerField()
    
    # 코 부위의 세 번째 ID
    nose_parts_id3 = models.BigIntegerField()

    # 코 부위의 네 번째 ID
    nose_parts_id4 = models.BigIntegerField()
    
    # 조합에 대한 설명 (최대 500자)
    desc = models.CharField(max_length=500)

    class Meta:

        db_table = 'nose_comb'
        managed = False
  
      
class ChinComb(models.Model):
    # chin_comb_id는 기본 키(Primary Key)로 자동 증가(Auto Increment)하는 BigIntegerField입니다.
    chin_comb_id = models.BigAutoField(primary_key=True) 
    
    # 턱 부위의 첫 번째 ID (예: 턱 형태)
    chin_parts_id1 = models.BigIntegerField()
    
    # 턱 부위의 두 번째 ID (예: 턱 길이)
    # 이 필드는 데이터가 없을 경우 NULL을 허용해야 하므로 null=True, blank=True를 설정합니다.
    chin_parts_id2 = models.BigIntegerField(null=True, blank=True)
    
    # 조합에 대한 설명 (최대 500자)
    desc = models.CharField(max_length=500)

    class Meta:

        db_table = 'chin_comb'
        managed = False 

class ChatRoom(models.Model):
    room_id = models.BigAutoField(primary_key=True)  # 자동 증가 기본 키
    user1 = models.ForeignKey(User, on_delete=models.CASCADE, related_name='chat_user1')
    user2 = models.ForeignKey(User, on_delete=models.CASCADE, related_name='chat_user2')
    created_at = models.DateTimeField(auto_now_add=True)
    room_string_id = models.CharField(max_length=255, unique=True)
    user1_selected_at = models.DateTimeField(null=True, blank=True)
    user2_selected_at = models.DateTimeField(null=True, blank=True)
    user1_selected = models.BooleanField(default=False)
    user2_selected = models.BooleanField(default=False)

    class Meta:
        db_table = 'chat_room'  # ✅ 실제 DB 테이블명에 맞춤

class Message(models.Model):
    message_id = models.BigAutoField(primary_key=True)
    room_id = models.BigIntegerField(db_index=True)
    sender_id = models.BigIntegerField()
    content = models.TextField(null=True, blank=True)
    type = models.CharField(max_length=10)  # 'TEXT','IMG','VIDEO'
    send_at = models.DateTimeField(db_index=True)
    is_read = models.BooleanField(default=False)
    read_at = models.DateTimeField(null=True, blank=True)
    receiver_id = models.BigIntegerField(null=True, blank=True)

    class Meta:
        db_table = "message"
        managed = False
        indexes = [models.Index(fields=["room_id", "send_at"])]