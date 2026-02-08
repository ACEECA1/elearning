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

    $('#openModalBtn').click(function() {
        $('#editCourseModal').fadeIn();
    });

    $('#closeModalBtn').click(function() {
        $('#editCourseModal').fadeOut();
    });

    $('#changeImageBtn').click(function() {
        $('#thumbnailInput').click();
    });

    $('#thumbnailInput').change(function() {
        var fichier = $(this).val();
        if (fichier != "") {
            alert("Nouvelle image sélectionnée : " + fichier);
        }
    });

    $('#editCourseForm').submit(function(e) {
        e.preventDefault();
        
        var titre = $('#courseTitle').val();
        var audience = $('#targetAudience').val();

        if (titre == "") {
            alert("Erreur : Le titre du cours ne peut pas être vide.");
        } else {
            alert("Modifications enregistrées pour le cours : " + titre);
            $('#editCourseModal').fadeOut();
        }
    });

    $('.sidebar-link').click(function() {
        $('.sidebar-link').removeClass('active');
        $(this).addClass('active');
    });

    console.log("Page d'édition de cours chargée");
});