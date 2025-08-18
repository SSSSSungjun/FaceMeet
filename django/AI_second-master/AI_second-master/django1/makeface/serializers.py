from rest_framework import serializers


class FaceCompositionDbidSerializer(serializers.Serializer):
    face = serializers.ListField(child=serializers.IntegerField(min_value=1), required=False)
    eye = serializers.ListField(child=serializers.IntegerField(min_value=1), required=False)
    brow = serializers.ListField(child=serializers.IntegerField(min_value=1), required=False)
    nose = serializers.ListField(child=serializers.IntegerField(min_value=1), required=False)
    mouth = serializers.ListField(child=serializers.IntegerField(min_value=1), required=False)
    chin = serializers.ListField(child=serializers.IntegerField(min_value=1), required=False)

    def to_internal_value(self, data):
        # 요청 데이터에서 비어있는 리스트를 가진 키는 제거합니다.
        return {key: value for key, value in super().to_internal_value(data).items() if value}