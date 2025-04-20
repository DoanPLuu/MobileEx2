from accounts.models import User
from music.models import Playlist, Song

# Thay 'admin' bằng username có thật
user = User.objects.get(username='testuser')

playlist = Playlists.objects.create(
    name="My Favorite Songs",
    user=user,
    cover_image="https://via.placeholder.com/150",
    is_public=False
)

song = Song.objects.get(id=1)  # Đảm bảo bài hát này tồn tại
playlist.songs.add(song)
