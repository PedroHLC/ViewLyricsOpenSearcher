<?php
 /*
  * ViewLyrics Open Searcher
  * Developed by PedroHLC
  * Last update: 09-08-2012
  */

$search_url = "http://search.crintsoft.com/searchlyrics.htm";
$search_query_base = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?><search filetype=\"lyrics\" artist=\"%s\" title=\"%s\" client=\"MiniLyrics\" />";
$search_useragent = "MiniLyrics";
$search_md5watermark = "Mlv1clt4.0";

if(isset($_GET["title"])){
  $title = $_GET["title"];
}else{
  $title = "";
}

if(isset($_GET["artist"])){
  $artist = $_GET["artist"];
}else{
  $artist = "";
}

if(strlen($title) < 1 && strlen($artist) < 1){
  print "<html><body><form action=\"#\" method=\"GET\">Artist: <input name=\"artist\" /><br />Title: <input name=\"title\" /><br /><input type=\"submit\" /></form></body></html>";
  exit(0);
}

$search_query = sprintf($search_query_base, $artist, $title);

function hexToStr($hex)
{
    $string='';
    for ($i=0; $i < strlen($hex)-1; $i+=2)
    {
        $string .= chr(hexdec($hex[$i].$hex[$i+1]));
    }
    return $string;
}

function vl_enc($data, $md5_extra){
  $datalen = strlen($data);
  $hasheddata= hexToStr(md5($data.$md5_extra));
  $j = 0;
  for($i = 0; $i < $datalen; $i++){
    $j += ord($data[$i]);
  }
  $magickey = chr(round($j / $datalen));
  $encddata = $data;
  for($i = 0; $i < $datalen; $i++){
    $encddata[$i] = ($data[$i] ^ $magickey);
  }
  $result ="\x02".$magickey."\x04\x00\x00\x00".$hasheddata.$encddata;
  return $result;
}

$search_encquery = vl_enc($search_query, $search_md5watermark);

function http_post($url, $data, $ua){
  $params = array('http' => array(
              'method' => 'POST',
              'user_agent' => $ua,
              'protocol_version' => 1.1,
              'header' => array(
                'Connection: Keep-Alive',
                'Expect: 100-continue',
              ),
              'content' => $data
            ));
  $context = stream_context_create($params);
  $stream = fopen($url, 'rb', false, $context);
  $response = stream_get_contents($stream);
  fclose($stream);
  return $response;
}

$search_result = http_post($search_url, $search_encquery, $search_useragent);

function vl_dec($data){
  $magickey = $data[1];
  $result = "";
  for($i = 22, $datalen = strlen($data); $i < $datalen; $i++){
    $result .= ($data[$i] ^ $magickey);
  }
  return $result;
}

if(!$search_result){
  print "FAILED";
}else{
  echo vl_dec($search_result);
  print "\nThis page uses XML, you can see it in this file source (Ctrl+U).";
}
?>
