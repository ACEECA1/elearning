$(document).ready(function() {

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
                alert("Recherche globale : " + texte);
            }
        }
    });

    $('#enrollBtn').click(function() {
        var cle = $('#enrollmentKey').val();
        
        if (cle == "") {
            alert("Veuillez entrer une clé d'inscription.");
        } else {
            alert("Inscription réussie avec la clé : " + cle);
            $('#enrollmentKey').val("");
        }
    });

    $('.course-btn').click(function(e) {
        // Empêcher le comportement par défaut si c'est un lien <a>
        if ($(this).is('a')) {
            // On laisse le lien fonctionner normalement pour la navigation
        } else {
            var carte = $(this).parents('.course-card');
            var titre = carte.find('.course-title').text();
            alert("Lancement du cours : " + titre);
        }
    });

    $('.sidebar-link').click(function() {
        $('.sidebar-link').removeClass('active');
        $(this).addClass('active');
    });

    // Gestion du menu mobile si le bouton est ajouté ultérieurement
    $('#mobileSidebarToggle').click(function() {
        var sidebar = $('#sidebar');
        if (sidebar.is(':visible')) {
            sidebar.hide();
        } else {
            sidebar.show();
        }
    });

    console.log("Tableau de bord étudiant chargé");
});