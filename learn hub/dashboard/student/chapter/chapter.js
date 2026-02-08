$(document).ready(function() {

    var sidebar = $('#sidebar');
    
    $('#mobileSidebarToggle').click(function() {
        if (sidebar.is(':visible')) {
            sidebar.hide();
        } else {
            sidebar.show();
        }
    });

    $('#notificationBtn').click(function() {
        var btn = $(this);
        btn.animate({ top: "-5px" }, "fast");
        btn.animate({ top: "5px" }, "fast");
        btn.animate({ top: "0px" }, "fast");
    });

    $('.search-bar input').keypress(function(e) {
        if (e.which == 13) {
            var texte = $(this).val();
            if (texte != "") {
                alert("Recherche dans le cours : " + texte);
            }
        }
    });
    var estEnLecture = false;
    var progression = 0;
    var timer;
    function demarrerVideo() {
        estEnLecture = true;
        $('#playButton').fadeOut();
        $('#playPauseBtn i').removeClass('fa-play');
        $('#playPauseBtn i').addClass('fa-pause');
        
        timer = setInterval(function() {
            progression = progression + 1;
            if (progression > 100) {
                progression = 0;
            }
            $('#progressFilled').css('width', progression + '%');
        }, 100);
    }
    function arreterVideo() {estEnLecture = false;
        $('#playButton').fadeIn();
        $('#playPauseBtn i').removeClass('fa-pause');
        $('#playPauseBtn i').addClass('fa-play');clearInterval(timer);}
$('#playButton').click(function() {demarrerVideo();});
$('#playPauseBtn').click(function() {if (estEnLecture) {arreterVideo();}else {demarrerVideo();}});
$('#volumeBtn').click(function() {alert("Volume activé");});
$('#settingsBtn').click(function() {alert("Ouverture des paramètres vidéo");});
$('#fullscreenBtn').click(function() {alert("Passage en plein écran");});
    console.log("Chapitre chargé");
});