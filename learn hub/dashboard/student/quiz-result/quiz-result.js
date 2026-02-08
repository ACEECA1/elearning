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
                alert("Recherche : " + texte);
            }
        }
    });

    $('#backBtn').click(function() {
        alert("Retour au cours");
    });

    var noteCible = 16; 
    var pourcentageCible = 80;
    var compteur = 0;
    var rayon = 120;
    var circonference = 2 * Math.PI * rayon;

    var cercle = $('#progressCircle');
    var textePourcentage = $('#gradePercentage');
    var texteScore = $('#scoreEarned');

    cercle.css('stroke-dasharray', circonference);
    cercle.css('stroke-dashoffset', circonference);

    var timer = setInterval(function() {
        compteur = compteur + 1;

        textePourcentage.text(compteur + "%");
        
        var scoreCourant = Math.round((compteur / 100) * 20);
        texteScore.text(scoreCourant);

        var offset = circonference - ((compteur / 100) * circonference);
        cercle.css('stroke-dashoffset', offset);

        if (compteur >= pourcentageCible) {
            clearInterval(timer);
            if (pourcentageCible >= 50) {
                $('.result-subtitle').text("Félicitations ! Vous avez réussi.");
                $('.result-subtitle').css('color', '#4a3bbf');
            }
        }
    }, 20);

    $('#continueBtn').click(function() {
        alert("Chargement du module suivant...");
    });

    console.log("Page de résultats chargée");
});