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
        alert("Retour au tableau de bord");
    });

    $('#chapterForm').submit(function(e) {
        e.preventDefault(); 
        
        var titre = $('#chapterTitle').val();
        var description = $('#chapterDescription').val();

        if (titre == "") {
            alert("Erreur : Le titre est obligatoire.");
        } else {
            alert("Les modifications du chapitre '" + titre + "' ont été enregistrées.");
        }
    });

    $('.support-item').click(function() {
        var nomSupport = $(this).find('.support-title').text();
        var typeSupport = $(this).find('.support-type').text();
        
        alert("Ouverture du support : " + nomSupport + " (" + typeSupport + ")");
    });

    $('.support-item').hover(
        function() {
            $(this).css('background-color', '#f9fafb');
            $(this).css('cursor', 'pointer');
        }, 
        function() {
            $(this).css('background-color', '');
        }
    );

    console.log("Page création de chapitre chargée");
});