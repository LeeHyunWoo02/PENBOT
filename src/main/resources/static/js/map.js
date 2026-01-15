document.addEventListener('DOMContentLoaded', () => {

    const pensionLat = 37.207518;
    const pensionLng = 126.568463;
    const pensionPosition = new naver.maps.LatLng(pensionLat, pensionLng);

    const mapOptions = {
        center: pensionPosition,
        zoom: 15,
        minZoom: 10,
        zoomControl: true,
        zoomControlOptions: {
            position: naver.maps.Position.TOP_RIGHT
        }
    };

    const map = new naver.maps.Map('map', mapOptions);

    const marker = new naver.maps.Marker({
        position: pensionPosition,
        map: map,
        title: '라온아띠 키즈 풀빌라',
        icon: {
            content: `
                <div style="background:var(--primary-color); color:white; padding:8px 12px; border-radius:20px; font-weight:bold; box-shadow:0 2px 5px rgba(0,0,0,0.3); display:flex; align-items:center; gap:5px;">
                    <i class="fa-solid fa-location-dot"></i> 라온아띠
                    <div style="position:absolute; bottom:-6px; left:50%; transform:translateX(-50%); width:0; height:0; border-left:6px solid transparent; border-right:6px solid transparent; border-top:6px solid var(--primary-color);"></div>
                </div>
            `,
            anchor: new naver.maps.Point(40, 40)
        }
    });

    const contentString = [
        '<div style="padding:15px; min-width:200px; text-align:center;">',
        '   <h4 style="margin-bottom:5px; font-weight:bold;">라온아띠 키즈 풀빌라</h4>',
        '   <p style="font-size:13px; color:#666;">경기도 안산시 단원구 ...</p>',
        '   <a href="https://map.naver.com" target="_blank" style="color:blue; font-size:12px;">네이버 지도에서 보기</a>',
        '</div>'
    ].join('');

    const infowindow = new naver.maps.InfoWindow({
        content: contentString,
        maxWidth: 300,
        backgroundColor: "#fff",
        borderColor: "#ccc",
        borderWidth: 1,
        anchorSize: new naver.maps.Size(10, 10),
        anchorSkew: true,
        anchorColor: "#fff",
        pixelOffset: new naver.maps.Point(0, -10)
    });

    naver.maps.Event.addListener(marker, "click", function(e) {
        if (infowindow.getMap()) {
            infowindow.close();
        } else {
            infowindow.open(map, marker);
        }
    });

    // infowindow.open(map, marker);
});