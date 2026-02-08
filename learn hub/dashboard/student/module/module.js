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
                alert("Recherche dans le module : " + texte);
            }
        }
    });

    $('#backBtn').click(function() {
        alert("Retour vers la page du cours");
    });

    $('.chapter-card').click(function() {
        var carte = $(this);
        
        if (carte.find('.locked-icon').length > 0) {
            alert("Ce chapitre est verrouillé. Veuillez terminer les précédents.");
        } else {
            var numero = carte.attr('data-chapter');
            var titre = carte.find('.chapter-title').text();
            alert("Ouverture du Chapitre " + numero + " : " + titre);
        }
    });

    $('.chapter-card').mouseenter(function() {
        $(this).css('background-color', '#f9fafb');
    });

    $('.chapter-card').mouseleave(function() {
        $(this).css('background-color', '');
    });

    console.log("Liste des chapitres chargée");
});